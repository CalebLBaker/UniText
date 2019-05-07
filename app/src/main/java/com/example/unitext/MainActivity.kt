package com.example.unitext

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.view.View
import android.widget.EditText
import android.widget.Toast
import java.lang.Exception

const val PERMISSION_REQUEST_SEND_SMS = 1
const val DESTINATION = "3197593722"
const val PDU_TYPE = "pdus"
class MainActivity : AppCompatActivity() {

    private val messages = arrayListOf(Message("Hello there.", "Bob"), Message("How are you?", "You"), Message("I am fine.", "Bob"))
    private val adapter = MessageAdapter(messages)
    private var messageList : RecyclerView? = null
    private val smsManager = SmsManager.getDefault()
    internal var smsReceiver : BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerReceiver()
        messageList = findViewById(R.id.message_list)
        messageList!!.adapter = adapter
        messageList!!.layoutManager = LinearLayoutManager(this)
        actionBar?.title = "Bob"
        supportActionBar?.title = "Bob"
    }

    fun sendMessage(view : View) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
         != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                                              arrayOf(android.Manifest.permission.SEND_SMS),
                                              PERMISSION_REQUEST_SEND_SMS)
        }
        else {
            sendSms()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_SEND_SMS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    sendSms()
                }
                else {
                    // permission denied
                }
                return
            }
            else -> {
                return
            }
        }
    }

    private fun sendSms() {
        val editText = findViewById<EditText>(R.id.editText).text
        val message = editText.toString()
        val messageParts = smsManager.divideMessage(message)
        messageParts.forEach {
            try {
                SmsManager.getDefault().sendTextMessage(
                    DESTINATION, null, it,
                    null, null
                )
                addMessage(Message(it, getString(R.string.you)))
                editText.clear()
            }
            catch (e : Exception) {
                Toast.makeText(applicationContext, getString(R.string.fail), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addMessage(msg : Message) {
        val newPos = messages.size
        messages.add(msg)
        adapter.notifyItemInserted(newPos)
        messageList!!.scrollToPosition(newPos)
    }

    fun registerReceiver() {
        smsReceiver = object : BroadcastReceiver() {

            @TargetApi(Build.VERSION_CODES.M)
            override fun onReceive(context: Context, intent: Intent) {
                val bundle = intent.extras
                if (bundle != null) {
                    val msgs: Array<SmsMessage>
                    var strMessage = ""
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
                            addMessage(Message(msgs[j].messageBody, msgs[j].originatingAddress!!))
                        }
                    }
                }
            }
        }
        registerReceiver(smsReceiver, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    }

    override fun onStop() {
        super.onStop()
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver)
        }
    }

}
