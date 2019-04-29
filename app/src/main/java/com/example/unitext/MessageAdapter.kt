package com.example.unitext

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class MessageAdapter(private val messages : List<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val textView = itemView.findViewById(R.id.message_text) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.ViewHolder {
        val messageView = LayoutInflater.from(parent.context).inflate(R.layout.message, parent, false)
        return ViewHolder(messageView)
    }

    override fun onBindViewHolder(viewHolder: MessageAdapter.ViewHolder, position: Int) {
        viewHolder.textView.text = messages[position].text
    }

    override fun getItemCount() = messages.size
}