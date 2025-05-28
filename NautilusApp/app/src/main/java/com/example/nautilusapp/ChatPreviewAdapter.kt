package com.example.nautilusapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatPreviewAdapter(private val chats: List<ChatPreviewData>, private val onClick: (ChatPreviewData) -> Unit) :
    RecyclerView.Adapter<ChatPreviewAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.image_profile)
        val name: TextView = view.findViewById(R.id.text_username)
        val prevMessage: TextView = view.findViewById(R.id.text_last_message)
        val time: TextView = view.findViewById(R.id.time_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_preview, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.profileImage.setImageResource(chat.profileImageResId)
        holder.name.text = chat.chatName
        holder.prevMessage.text = chat.lastMessage
        holder.time.text = chat.time
        holder.itemView.setOnClickListener { onClick(chat)}

    }

    override fun getItemCount(): Int = chats.size
}