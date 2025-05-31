package com.example.nautilusapp

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nautilusapp.Common.port
import com.example.nautilusapp.Common.servorIpAdress
import java.io.OutputStream
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.text.Charsets.US_ASCII

class ChatAdapter(private val messages: List<ChatMessageData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SENT = 1
    private val TYPE_RECEIVED = 2
    //TODO afficher les deux boutons quand le message est une friend request

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSentByMe) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = if (viewType == TYPE_SENT)
            R.layout.item_chat_sent else R.layout.item_chat_received //TODO les messages envoyés ne s'affichent pas

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val textView = holder.itemView.findViewById<TextView>(R.id.message_text)
        textView.text = messages[position].message
    }

    fun onFriendRequestAccepted(){
        //TODO
        //We send a message to the servor to ask for the IP address of the user
    }
    override fun getItemCount() = messages.size
}
