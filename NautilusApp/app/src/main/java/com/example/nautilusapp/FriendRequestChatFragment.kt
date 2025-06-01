package com.example.nautilusapp

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nautilusapp.Common.me
import com.example.nautilusapp.Common.padding
import com.example.nautilusapp.Common.requestIdUsers
import java.io.OutputStream
import java.net.Socket
import java.time.LocalDate
import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.toString
import kotlin.concurrent.thread
import kotlin.text.Charsets.US_ASCII

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendRequestChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendRequestChatFragment(val discussion: Int,val chatName: String) : Fragment() {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView

    private var messages = mutableListOf<ChatMessageData>() // Dummy messages

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_request_chat, container, false)
    }
    //TODO drop the friend request message when accepting it

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view)
        messageInput = view.findViewById(R.id.message_input)
        sendButton = view.findViewById(R.id.button_send)
        val name = view.findViewById<TextView>(R.id.chat_name)
        name.text = chatName

        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.adapter = chatAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val dbHelper = DatabaseHelper(requireContext())
        var db = dbHelper.readableDatabase
        var dbWrite = dbHelper.readableDatabase

        val cursor = db.rawQuery("Select text, author From Message Where idDiscussion=$discussion",null,null)
        while (cursor.moveToNext()){
            messages.add(ChatMessageData(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Message.COLUMN_NAME_COL1)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Message.COLUMN_NAME_COL4)) == me))
        }
        cursor.close()

        chatAdapter.notifyDataSetChanged()

        val prompt = view.findViewById<TextView>(R.id.prompt)
        prompt.text = "$chatName sent you a friend request !"
        val yes = view.findViewById<Button>(R.id.button_accept)
        yes.setOnClickListener(View.OnClickListener{
            //get the id of a User
            var cursor = db.rawQuery("Select t1.mailAdress From Talk_in As t1 Join Talk_in As t2 On t1.idDiscussion=t2.idDiscussion Where t2.idDiscussion='$discussion';",null,null)
            var stop: Boolean = false
            var data: String = ""
            while (cursor.moveToNext() && !(stop) ){
                data = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL1))
                if(data != me){
                    stop = true
                }
            }
            cursor.close()

            Log.d("Discussion",data)

            thread{
                //get the address of the User
                val addr = requestIdUsers(data)

                Log.d("Ip",addr)

                var client = Socket(addr,1895)
                val writer: OutputStream = client.getOutputStream()
                writer.write(("2").toByteArray(US_ASCII))

                var msg = (me).toByteArray(US_ASCII)
                var size = msg.size.toString()
                var padding = padding(size, 3 - size.length)
                var finalSize: ByteArray = padding.toByteArray((US_ASCII))
                writer.write(finalSize)
                writer.write(msg)

                cursor = db.rawQuery("Select firstName, LastName, university From Simplified_User Where mailAdress='$me';",null,null)
                cursor.moveToNext()
                val firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL2))
                val lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL3))
                val university = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL4))
                cursor.close()

                msg = (firstName).toByteArray(US_ASCII)
                writer.write(padding(msg.size.toString(),3-msg.size.toString().length).toByteArray((US_ASCII)))
                writer.write(msg)

                msg = (lastName).toByteArray(US_ASCII)
                writer.write(padding(msg.size.toString(),3-msg.size.toString().length).toByteArray((US_ASCII)))
                writer.write(msg)


                msg = (university).toByteArray(US_ASCII)
                writer.write(padding(msg.size.toString(),3-msg.size.toString().length).toByteArray((US_ASCII)))
                writer.write(msg)

                client.close()
            }

            var textMessage = "$chatName added you as a friend ! <3"
            Log.d("Message",textMessage)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            var current = LocalDate.now().format(formatter)
            Log.d("did it worked ??",current.toString())

            //new we try to get the hour
            val zoneId = systemDefault()
            val currentZonedDateTime = ZonedDateTime.now(zoneId)
            val currentTimestamp: Long = currentZonedDateTime.toInstant().toEpochMilli()
            Log.d("Time ?",currentTimestamp.toString())
            val hour = (currentTimestamp.mod(3600*1000*24)+2*3600*1000)/(3600*1000)
            val min = (currentTimestamp.mod(3600*1000))/(60*1000)
            val sec = currentTimestamp.mod(60*1000)/1000
            Log.d("Time ?", "$hour:$min:$sec")

            val values = ContentValues().apply {
                put(DatabaseContract.Message.COLUMN_NAME_COL1, textMessage)
                put(DatabaseContract.Message.COLUMN_NAME_COL2, current.toString())
                put(DatabaseContract.Message.COLUMN_NAME_COL3, "$hour:$min:$sec")
                put(DatabaseContract.Message.COLUMN_NAME_COL4,me)
                put(DatabaseContract.Message.COLUMN_NAME_COL5, discussion)
            }

            dbWrite.insert(DatabaseContract.Message.TABLE_NAME, null, values)

            messages = mutableListOf<ChatMessageData>()

            cursor = db.rawQuery("Select text, author From Message Where idDiscussion=$discussion",null,null)
            while (cursor.moveToNext()){
                messages.add(ChatMessageData(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Message.COLUMN_NAME_COL1)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Message.COLUMN_NAME_COL4)) == me))
            }
            cursor.close()

            chatAdapter.notifyDataSetChanged()
        })

    }

}