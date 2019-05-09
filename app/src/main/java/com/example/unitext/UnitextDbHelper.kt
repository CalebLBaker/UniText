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
        val contactValues = ContentValues().apply {
            put(DatabaseConstants.Contact.NUMBER, "3197593722")
            put(DatabaseConstants.Contact.NAME, "Caleb")
        }
        db.insert(DatabaseConstants.Contact.TABLE_NAME, null, contactValues)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DatabaseConstants.DELETE_MESSAGE_TABLE)
        db.execSQL(DatabaseConstants.DELETE_CONTACT_TABLE)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun getContact(id : Long) : Contact? {
        val cursor =  readableDatabase.query(DatabaseConstants.Contact.TABLE_NAME,
                                             arrayOf(DatabaseConstants.Contact.NAME, DatabaseConstants.Contact.NUMBER),
                                            "${BaseColumns._ID} = ?",
                                             arrayOf(id.toString()),
                                             null, null, null)
        return with (cursor) {
            if (moveToNext()) {
                Contact(getString(getColumnIndexOrThrow(DatabaseConstants.Contact.NAME)), getString(getColumnIndexOrThrow(DatabaseConstants.Contact.NUMBER)), id)
            } else {
                null
            }
        }
    }

    private fun insertContact(name : String, number : String?) : Long {
        val contactValues = ContentValues().apply {
            put(DatabaseConstants.Contact.NUMBER, number)
            put(DatabaseConstants.Contact.NAME, name)
        }
        return writableDatabase.insert(DatabaseConstants.Contact.TABLE_NAME, null, contactValues)
    }

    fun insertMessage(text : String, time : Date, contact : Long, inbound : Boolean) {
        val msgValues = ContentValues().apply {
            put(DatabaseConstants.Message.CONVERSATION, contact)
            put(DatabaseConstants.Message.TEXT, text)
            put(DatabaseConstants.Message.TIME, time.time)
            put(DatabaseConstants.Message.INBOUND, inbound)
        }
        writableDatabase.insert(DatabaseConstants.Message.TABLE_NAME, null, msgValues)
    }

    fun insertMessageWithNumber(text : String, time : Date, contactNumber : String, inbound : Boolean) : Contact {
        val db = writableDatabase
        val cursor = db.query(DatabaseConstants.Contact.TABLE_NAME,
            arrayOf(BaseColumns._ID, DatabaseConstants.Contact.NAME),
            "${DatabaseConstants.Contact.NUMBER} = ?",
            arrayOf(contactNumber),
            null, null, null)
        var contactName = contactNumber
        val contactId = if (cursor.moveToNext()) {
            contactName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.Contact.NAME))
            cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
        }
        else {
            insertContact(contactName, contactNumber)
        }
        cursor.close()
        insertMessage(text, time, contactId, inbound)
        return Contact(contactName, contactNumber, contactId)
    }

    fun query(contact : Long) : ArrayList<Message> {
        val cursor = readableDatabase.query("""${DatabaseConstants.Message.TABLE_NAME} JOIN ${DatabaseConstants.Contact.TABLE_NAME}
                                               ON ${DatabaseConstants.Message.TABLE_NAME}.${DatabaseConstants.Message.CONVERSATION} = ${DatabaseConstants.Contact.TABLE_NAME}.${BaseColumns._ID}""",
                                            arrayOf(DatabaseConstants.Message.TEXT, DatabaseConstants.Message.TIME, DatabaseConstants.Contact.NAME, DatabaseConstants.Contact.NUMBER),
                                            "${DatabaseConstants.Message.CONVERSATION} = ?",
                                            arrayOf(contact.toString()),
                                            null, null,
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

    fun queryRecent(lastMessage : Date, contact : Long) : ArrayList<Message> {
        val cursor = readableDatabase.query("""${DatabaseConstants.Message.TABLE_NAME} JOIN ${DatabaseConstants.Contact.TABLE_NAME}
                                               ON ${DatabaseConstants.Message.TABLE_NAME}.${DatabaseConstants.Message.CONVERSATION} = ${DatabaseConstants.Contact.TABLE_NAME}.${BaseColumns._ID}""",
                                            arrayOf(DatabaseConstants.Message.TEXT, DatabaseConstants.Message.TIME, DatabaseConstants.Contact.NAME, DatabaseConstants.Contact.NUMBER),
                                            "${DatabaseConstants.Message.TIME} > ? AND ${DatabaseConstants.Message.CONVERSATION} = ?",
                                            arrayOf(lastMessage.time.toString(), contact.toString()),
                                            null, null,
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

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "unitext.db"
    }
}