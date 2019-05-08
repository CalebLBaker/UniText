package com.example.unitext

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage

const val NEW_SMS_INTENT = "com.example.unitext.NEW_SMS"
const val BODY = "body"
const val SENDER = "sender"
const val TIME = "time"

class SmsReceiver : BroadcastReceiver() {

    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras
        if (bundle != null) {
            val msgs: Array<SmsMessage>
            val format = bundle.getString("format")
            val pdus = bundle.get(PDU_TYPE) as Array<*>?
            if (pdus != null) {
                val isVersionM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                var i = 0
                msgs = Array(pdus.size) {
                    if (isVersionM) {
                        SmsMessage.createFromPdu(pdus[i++] as ByteArray, format)
                    } else {
                        SmsMessage.createFromPdu(pdus[i++] as ByteArray)
                    }
                }
                for (j in 0 until msgs.size) {
                    val msg = Message(msgs[j].messageBody, msgs[j].originatingAddress!!)
                    val sender = UnitextDbHelper(context).insertWithNumber(msg)
                    val newIntent = Intent(NEW_SMS_INTENT)
                    val extras = Bundle()
                    extras.putString(BODY, msg.text)
                    extras.putString(SENDER, sender)
                    extras.putLong(TIME, msg.time.time)
                    newIntent.putExtras(extras)
                    context.sendBroadcast(newIntent)
                }
            }
        }
    }
}
