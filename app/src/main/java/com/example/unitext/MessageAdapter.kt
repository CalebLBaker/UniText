package com.example.unitext

import android.provider.CalendarContract
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import java.util.*

class MessageAdapter(private val messages : List<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val textView = itemView.findViewById(R.id.message_text) as TextView
        val senderView = itemView.findViewById(R.id.message_sender) as TextView
        val timeView = itemView.findViewById(R.id.message_time) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.ViewHolder {
        val messageView = LayoutInflater.from(parent.context).inflate(R.layout.message, parent, false)
        return ViewHolder(messageView)
    }

    override fun onBindViewHolder(viewHolder: MessageAdapter.ViewHolder, position: Int) {
        val msg = messages[position]
        viewHolder.textView.text = msg.text
        viewHolder.senderView.text = msg.sender
        val prev = if (position != 0) {
            messages[position-1].time
        }
        else {
            val cal = Calendar.getInstance()
            cal.time = Date(0)
            cal
        }
        val locale = Locale.getDefault()
        val currentDate = msg.time
        var year = ""
        var day = ""
        var time = ""
        var pm = ""
        var showing = false
        val currYear = currentDate.get(Calendar.YEAR)
        if (currYear != prev.get(Calendar.YEAR)) {
            year = ", $currYear "
            showing = true
        }
        val currDay = currentDate.get(Calendar.DAY_OF_MONTH)
        if (showing || currDay != prev.get(Calendar.DAY_OF_MONTH) || currentDate.get(Calendar.MONTH) != prev.get(Calendar.MONTH)) {
            day = currentDate.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale) + " " + currDay.toString()
            if (!showing) day += " "
            showing = true
        }
        if (showing || currentDate.get(Calendar.AM_PM) != prev.get(Calendar.AM_PM)) {
            pm = " " + currentDate.getDisplayName(Calendar.AM_PM, Calendar.SHORT, locale)
            showing = true
        }
        val currHour = currentDate.get(Calendar.HOUR)
        val currMinute = currentDate.get(Calendar.MINUTE)
        if (showing || currMinute != prev.get(Calendar.MINUTE) || currHour != prev.get(Calendar.HOUR)) {
            time = "$currHour:$currMinute"
        }
        viewHolder.timeView.text = day + year + time + pm
    }

    override fun getItemCount() = messages.size
}