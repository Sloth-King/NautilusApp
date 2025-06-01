package com.example.nautilusapp

import android.content.Intent
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nautilusapp.Common.me
import com.example.nautilusapp.Common.padding
import com.example.nautilusapp.Common.port
import com.example.nautilusapp.Common.requestIdUsers
import com.example.nautilusapp.Common.servorIpAdress
import com.example.nautilusapp.DatabaseContract.Friend
import com.example.nautilusapp.DatabaseContract.Talk_in
import com.example.nautilusapp.DatabaseContract.User
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.text.Charsets.US_ASCII

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MessagePreviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessagePreviewFragment : Fragment() {

    private lateinit var storyAdapter: StoryAdapter
    private lateinit var chatAdapter: ChatPreviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Replace 'fragment_message_preview' with your actual layout resource name
        return inflater.inflate(R.layout.fragment_message_preview, container, false)
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment,"Chat")
            .addToBackStack(null)
            .commit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO : Replace it with actual data from the DB
        // YES I KNOW .... I'm on it
        val stories = listOf(
            StoryData("Lucieng", R.drawable.account),
            StoryData("The Goat (Andrew)", R.drawable.account),
            StoryData("John", R.drawable.account),
            StoryData("OmarEc", R.drawable.account),
        )


        val dbHelper = DatabaseHelper(requireContext())
        var db = dbHelper.readableDatabase

        // Add friend button
        val newChatButton = view.findViewById<Button>(R.id.button_new_chat)
        newChatButton.setOnClickListener{
            val dialog = AddFriendPopupFragment()
            dialog.show(childFragmentManager, "AddFriendPopup")

        }

        var cursor = db.rawQuery(
            "Select DISTINCT s.firstName, m.text, m.hour, d._id From Simplified_User AS s Join Talk_IN As t1 On s.mailAdress=t2.mailAdress Join Discussion as d On t1.idDiscussion=d._id Join Talk_In as t2 On t2.idDiscussion=d._id Join Message as m On m.idDiscussion=d._id Where t1.mailAdress=? And t1.mailAdress<>t2.mailAdress " +
                    "And m.Date>=(Select Max(m1.Date) From Message as m1 Where m1.idDiscussion=d._id) And m.hour>=(Select Max(m2.hour) From Message as m2 Where m2.idDiscussion=d._id And m2.hour) " +
                    "Order By m.Date Desc, m.Hour Limit 10;",arrayOf(me),null)

        //Ici la requête n'est pas bonne car elle renvoie toutes les lignes avec même discussion mais chaque message différent TODO a règler

        var chatPreview: ArrayList<ChatPreviewData> = arrayListOf()
        var cpt=0
        while (cursor.moveToNext()){
            cpt = cpt + 1
            var firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL2))
            var text = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Message.COLUMN_NAME_COL1))
            var bool: Boolean
            bool = text.contains(" : is sending you a friend request",false)
            Log.d("CursorData",firstName)
            chatPreview.add(ChatPreviewData(
                firstName, text,
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Message.COLUMN_NAME_COL3)),
                R.drawable.account,
                cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID)),bool))
        }

        Log.d("MessagePreview",cpt.toString())

        val chats = chatPreview.toList()

        cursor.close()

        // stories RecyclerView
        val recyclerStories = view.findViewById<RecyclerView>(R.id.storiesRecyclerView)
        recyclerStories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        storyAdapter = StoryAdapter(stories) { story ->
            Toast.makeText(requireContext(), "Clicked on story: ${story.username}", Toast.LENGTH_SHORT).show()
        }
        recyclerStories.adapter = storyAdapter

        // chat RecyclerView
        val recyclerChats = view.findViewById<RecyclerView>(R.id.chatsRecyclerView)
        recyclerChats.layoutManager = LinearLayoutManager(requireContext())
        chatAdapter = ChatPreviewAdapter(chats) { chat ->
            if(chat.isFriendRequest == true){
                openFragment(FriendRequestChatFragment(chat.id,chat.chatName))
            }
            else{
                openFragment(ChatFragment(chat.id,chat.chatName))
            }
        }
        recyclerChats.adapter = chatAdapter

    }

}
