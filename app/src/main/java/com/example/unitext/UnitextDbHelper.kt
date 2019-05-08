package com.example.unitext

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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



    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "unitext.db"
    }
}