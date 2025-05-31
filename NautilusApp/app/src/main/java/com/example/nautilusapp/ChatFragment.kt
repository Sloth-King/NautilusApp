package com.example.nautilusapp

import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nautilusapp.Common.me
import com.example.nautilusapp.Common.padding
import com.example.nautilusapp.Common.requestIdUsers
import java.io.OutputStream
import java.net.Socket
import kotlin.text.Charsets.US_ASCII

class ChatFragment(val discussion: Int,val chatName: String) : Fragment() {

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
        val name = view.findViewById<TextView>(R.id.chat_name)
        name.text = chatName

        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.adapter = chatAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Random test data
        val dbHelper = DatabaseHelper(requireContext())
        var db = dbHelper.readableDatabase

        val cursor = db.rawQuery("Select text, author From Message Where idDiscussion=$discussion",null,null)
        while (cursor.moveToNext()){
            messages.add(ChatMessageData(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Message.COLUMN_NAME_COL1)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Message.COLUMN_NAME_COL4)) == me))
        }
        cursor.close()

        chatAdapter.notifyDataSetChanged()

        sendButton.setOnClickListener {
            val text = messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                val dbHelper = DatabaseHelper(requireContext())
                var db = dbHelper.readableDatabase
                //get the id of the user with whom we communicate
                var cursor = db.rawQuery("Select mailAdress From Simplified_User As s Join Talk_in As t On s.mailAdress=t.mailAdress Where t.idDiscussion='$discussion';",null,null)
                var stop: Boolean = false
                var data: String = ""
                while (cursor.moveToNext() && !(stop) ){
                    data = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL1))
                    if(data != me){
                        stop = true
                    }
                }
                cursor.close()
                val addr = requestIdUsers(data)

                //then we send the message
                var client = Socket(addr,1895)
                val writer: OutputStream = client.getOutputStream()
                writer.write(("3").toByteArray(US_ASCII))

                //send the number of user in the conversation minus us (here it's 2-1)
                writer.write(padding("2",2-1).toByteArray(US_ASCII))

                var msg = (me).toByteArray(US_ASCII)
                var size = msg.size.toString()
                var padding = padding(size, 8 - size.length)
                var finalSize: ByteArray = padding.toByteArray((US_ASCII))
                writer.write(finalSize)
                writer.write(msg)

                //send the message
                size = text.toByteArray(US_ASCII).size.toString()
                writer.write(padding(size,8-size.length).toByteArray(US_ASCII))
                writer.write(text.toByteArray(US_ASCII))

                messages.add(ChatMessageData(text, true))
                chatAdapter.notifyItemInserted(messages.size - 1)
                chatRecyclerView.scrollToPosition(messages.size - 1)
                messageInput.setText("")
            }
        }

        fun addMessage(text: String){
            messages.add(ChatMessageData(text,false))
            chatAdapter.notifyDataSetChanged()
        }
    }
}
