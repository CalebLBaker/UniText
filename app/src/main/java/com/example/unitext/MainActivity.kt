package com.example.unitext

import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.telephony.SmsManager
import android.view.View
import android.widget.EditText

const val PERMISSION_REQUEST_SEND_SMS = 1
const val DESTINATION = "3197593722"
class MainActivity : AppCompatActivity() {

    val messages = arrayListOf(Message("Hello there.", "Bob"), Message("How are you?", "You"), Message("I am fine.", "Bob"))
    val adapter = MessageAdapter(messages)
    var messageList : RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        messageList = findViewById(R.id.message_list)
        messageList!!.adapter = adapter
        messageList!!.layoutManager = LinearLayoutManager(this)
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
        if (message != "") {
            SmsManager.getDefault().sendTextMessage(
                DESTINATION, null, message,
                null, null
            )
            val newPos = messages.size
            messages.add(Message(message, getString(R.string.you)))
            adapter.notifyItemInserted(newPos)
            messageList!!.scrollToPosition(newPos)

            editText.clear()
        }
    }

}
