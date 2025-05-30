package com.example.nautilusapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nautilusapp.Common.me
import com.example.nautilusapp.DatabaseContract.Friend
import com.example.nautilusapp.DatabaseContract.Talk_in
import com.example.nautilusapp.DatabaseContract.User

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
            .replace(R.id.fragment_container, fragment)
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

        var cursor = db.rawQuery(
            "Select DISTINCT s.firstName, m.text, m.hour, d._id From Simplified_User AS s Join Talk_IN As t1 On s.mailAdress=t2.mailAdress Join Discussion as d On t1.idDiscussion=d._id Join Talk_In as t2 On t2.idDiscussion=d._id Join Message as m On m.idDiscussion=d._id Where t1.mailAdress=?" +
                    "Order By m.Date Desc, m.Hour Limit 10;",arrayOf(me),null)

        var chatPreview: ArrayList<ChatPreviewData> = arrayListOf()
        while (cursor.moveToNext()){
            Log.d("CursorData",cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL2)))
            chatPreview.add(ChatPreviewData(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL2)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Message.COLUMN_NAME_COL1)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Message.COLUMN_NAME_COL3)),
                R.drawable.account))
        }

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
            openFragment(ChatFragment())
        }
        recyclerChats.adapter = chatAdapter
    }

}
