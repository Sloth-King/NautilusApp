package com.example.nautilusapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatFragment : Fragment() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView

    private val messages = mutableListOf<ChatMessageData>() // Dummy messages

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view)
        messageInput = view.findViewById(R.id.message_input)
        sendButton = view.findViewById(R.id.button_send)

        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.adapter = chatAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Random test data
        messages.add(ChatMessageData("Ayoooo", false))
        messages.add(ChatMessageData("Man f*ck you i'll see you at work", true))
        chatAdapter.notifyDataSetChanged()

        sendButton.setOnClickListener {
            val text = messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                messages.add(ChatMessageData(text, true))
                chatAdapter.notifyItemInserted(messages.size - 1)
                chatRecyclerView.scrollToPosition(messages.size - 1)
                messageInput.setText("")
            }
        }
    }
}
