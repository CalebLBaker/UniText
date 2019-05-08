package com.example.unitext

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import java.util.*

class UnitextDbHelper(context : Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DatabaseConstants.CREATE_CONTACT_TABLE)
        db.execSQL(DatabaseConstants.CREATE_MESSAGE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DatabaseConstants.DELETE_MESSAGE_TABLE)
        db.execSQL(DatabaseConstants.DELETE_CONTACT_TABLE)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun insertWithNumber(msg : Message) {
        val db = writableDatabase
        val cursor = db.query(DatabaseConstants.Contact.TABLE_NAME,
                              arrayOf(BaseColumns._ID),
                              "${DatabaseConstants.Contact.NUMBER} = ?",
                              arrayOf(msg.sender),
                              null, null, null)
        val sender = if (cursor.moveToNext()) {
            cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
        }
        else {
            val contactValues = ContentValues().apply {
                put(DatabaseConstants.Contact.NUMBER, msg.sender)
                put(DatabaseConstants.Contact.NAME, null as String?)
            }
            db.insert(DatabaseConstants.Contact.TABLE_NAME, null, contactValues)
        }
        cursor.close()
        insert(msg, sender)
    }

    fun insertWithName(msg : Message) {
        val db = writableDatabase
        val cursor = db.query(DatabaseConstants.Contact.TABLE_NAME,
            arrayOf(BaseColumns._ID),
            "${DatabaseConstants.Contact.NAME} = ?",
            arrayOf(msg.sender),
            null, null, null)
        val sender = if (cursor.moveToNext()) {
            cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
        }
        else {
            val contactValues = ContentValues().apply {
                put(DatabaseConstants.Contact.NAME, msg.sender)
                put(DatabaseConstants.Contact.NUMBER, null as String?)
            }
            db.insert(DatabaseConstants.Contact.TABLE_NAME, null, contactValues)
        }
        cursor.close()
        insert(msg, sender)
    }

    fun query() : ArrayList<Message> {
        val cursor = readableDatabase.query("""${DatabaseConstants.Message.TABLE_NAME} JOIN ${DatabaseConstants.Contact.TABLE_NAME}
                                               ON ${DatabaseConstants.Message.TABLE_NAME}.${DatabaseConstants.Message.SENDER} = ${DatabaseConstants.Contact.TABLE_NAME}.${BaseColumns._ID}""",
                                            arrayOf(DatabaseConstants.Message.TEXT, DatabaseConstants.Message.TIME, DatabaseConstants.Contact.NAME, DatabaseConstants.Contact.NUMBER),
                                            null, null, null, null,
                                            "${DatabaseConstants.Message.TIME} ASC")
        val ret = ArrayList<Message>()
        with(cursor) {
            while(moveToNext()) {
                var sender = getString(getColumnIndexOrThrow(DatabaseConstants.Contact.NAME))
                if (sender == null) {
                    sender = getString(getColumnIndexOrThrow(DatabaseConstants.Contact.NUMBER))
                }
                ret.add(Message(getString(getColumnIndexOrThrow(DatabaseConstants.Message.TEXT)),
                                sender,
                                Date(getLong(getColumnIndexOrThrow(DatabaseConstants.Message.TIME)))))
            }
        }
        return ret
    }

    private fun insert(msg : Message, sender: Long) {
        val msgValues = ContentValues().apply {
            put(DatabaseConstants.Message.SENDER, sender)
            put(DatabaseConstants.Message.TEXT, msg.text)
            put(DatabaseConstants.Message.TIME, msg.time.time)
        }
        writableDatabase.insert(DatabaseConstants.Message.TABLE_NAME, null, msgValues)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "unitext.db"
    }
}