package com.example.unitext

import android.provider.BaseColumns

object DatabaseConstants {

    object Contact {
        const val TABLE_NAME = "contact"
        const val NAME = "name"
        const val NUMBER = "number"
    }

    const val CREATE_CONTACT_TABLE = """
CREATE TABLE ${Contact.TABLE_NAME} (${BaseColumns._ID} INTEGER PRIMARY KEY,
                                    ${Contact.NAME} TEXT,
                                    ${Contact.NUMBER} TEXT)
"""

    const val DELETE_CONTACT_TABLE = """
DROP TABLE IF EXISTS ${Contact.TABLE_NAME}
"""

    object Message {
        const val TABLE_NAME = "message"
        const val TEXT = "content"
        const val CONVERSATION = "conversation"
        const val TIME = "time"
        const val INBOUND = "inbound"
    }

    const val CREATE_MESSAGE_TABLE = """
CREATE TABLE ${Message.TABLE_NAME} (${BaseColumns._ID} INTEGER PRIMARY KEY,
                                    ${Message.CONVERSATION} INTEGER,
                                    ${Message.INBOUND} BOOLEAN,
                                    ${Message.TEXT} TEXT,
                                    ${Message.TIME} DATETIME,
FOREIGN KEY(${Message.CONVERSATION}) REFERENCES ${Contact.TABLE_NAME}(${BaseColumns._ID}))
"""

    const val DELETE_MESSAGE_TABLE = """
DROP TABLE IF EXISTS ${Message.TABLE_NAME}
"""

}