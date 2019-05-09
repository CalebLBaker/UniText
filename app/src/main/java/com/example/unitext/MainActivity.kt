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
import android.view.View
import android.widget.EditText
import android.widget.Toast
import java.lang.Exception
import java.util.*

const val PERMISSION_REQUEST_SEND_SMS = 1
const val PDU_TYPE = "pdus"
const val RECIPIENT = 1L
class MainActivity : AppCompatActivity() {

    private var contact = Contact("")
    private var messageList : RecyclerView? = null
    private val smsManager = SmsManager.getDefault()
    private var smsReceiver : BroadcastReceiver? = null
    private var dbHelper : UnitextDbHelper? = null
    private var messages : ArrayList<Message>? = null
    private var adapter : MessageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = UnitextDbHelper(applicationContext)
        contact = dbHelper!!.getContact(RECIPIENT) ?: contact
        messages = dbHelper!!.query(contact.id)
        adapter = MessageAdapter(messages!!)
        registerReceiver()
        messageList = findViewById(R.id.message_list)
        messageList!!.adapter = adapter
        messageList!!.layoutManager = LinearLayoutManager(this)
        actionBar?.title = contact.name
        supportActionBar?.title = contact.name
        messageList!!.scrollToPosition(messages!!.size - 1)
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
                    Toast.makeText(applicationContext, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
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
                    contact.number, null, it,
                    null, null
                )
                val msg = Message(it, getString(R.string.you))
                dbHelper!!.insertMessage(msg.text, msg.time, contact.id, false)
                addMessage(msg)
                editText.clear()
            }
            catch (e : Exception) {
                Toast.makeText(applicationContext, getString(R.string.fail), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addMessage(msg : Message) {
        val newPos = messages!!.size
        messages!!.add(msg)
        adapter!!.notifyItemInserted(newPos)
        messageList!!.scrollToPosition(newPos)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(smsReceiver, IntentFilter(NEW_SMS_INTENT))

        val start = messages!!.size
        val newMessages = if (messages!!.size == 0) {
            dbHelper!!.query(contact.id)
        }
        else {
            dbHelper!!.queryRecent(messages!!.last().time, contact.id)
        }
        val numNew = newMessages.size
        if (numNew != 0) {
            messages!!.addAll(newMessages)
            adapter!!.notifyItemRangeInserted(start, numNew)
            messageList!!.scrollToPosition(start + numNew - 1)
        }

    }

    private fun registerReceiver() {
        smsReceiver = object : BroadcastReceiver() {

            @TargetApi(Build.VERSION_CODES.M)
            override fun onReceive(context: Context, intent: Intent) {
                val bundle = intent.extras
                if (bundle != null && bundle.getLong(SENDER_ID) == contact.id) {
                    val body = bundle.getString(BODY)
                    val senderName = bundle.getString(SENDER_NAME)
                    val time = bundle.getLong(TIME)
                    if (body != null && senderName != null) {
                        addMessage(Message(body, senderName, Date(time)))
                    }
                }
            }
        }
        registerReceiver(smsReceiver, IntentFilter(NEW_SMS_INTENT))
    }

    override fun onStop() {
        super.onStop()
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver)
        }
    }

}
