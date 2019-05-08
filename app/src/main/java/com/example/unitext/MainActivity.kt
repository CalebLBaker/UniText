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
import java.util.*

const val PERMISSION_REQUEST_SEND_SMS = 1
const val DESTINATION = "3197593722"
const val PDU_TYPE = "pdus"
class MainActivity : AppCompatActivity() {

    private var messageList : RecyclerView? = null
    private val smsManager = SmsManager.getDefault()
    private var smsReceiver : BroadcastReceiver? = null
    var dbHelper : UnitextDbHelper? = null
    private var messages : ArrayList<Message>? = null
    private var adapter : MessageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = UnitextDbHelper(applicationContext)
        messages = dbHelper!!.query()
        adapter = MessageAdapter(messages!!)
        registerReceiver()
        messageList = findViewById(R.id.message_list)
        messageList!!.adapter = adapter
        messageList!!.layoutManager = LinearLayoutManager(this)
        actionBar?.title = "Bob"
        supportActionBar?.title = "Bob"
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
                    DESTINATION, null, it,
                    null, null
                )
                val msg = Message(it, getString(R.string.you))
                dbHelper!!.insertWithName(msg)
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
        val newMessages = dbHelper!!.queryRecent(messages!!.last().time)
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
                if (bundle != null) {
                    val body = bundle.getString(BODY)
                    val sender = bundle.getString(SENDER)
                    val time = bundle.getLong(TIME)
                    if (body != null && sender != null) {
                        addMessage(Message(body, sender, Date(time)))
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
