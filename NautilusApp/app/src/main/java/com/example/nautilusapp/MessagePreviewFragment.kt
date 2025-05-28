package com.example.nautilusapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MessagePreviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessagesFragment : Fragment() {

    private lateinit var storyAdapter: StoryAdapter
    private lateinit var chatAdapter: ChatPreviewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Dummy data
        val stories = listOf(
            StoryData("Lucieng", R.drawable.account),
            StoryData("The Goat (Andrew)", R.drawable.account),
            StoryData("John", R.drawable.account),
            StoryData("OmarEc", R.drawable.account),
        )

        val chats = listOf(
            ChatPreviewData("Lucieng", "Mobile :(", "12:45", R.drawable.account),
            ChatPreviewData("The Goat (Andrew)", "Heyyy", "11:20", R.drawable.account),
            ChatPreviewData("OmarEc", "How come youon love me mane", "Yesterday", R.drawable.account),
            ChatPreviewData("John", "John", "Mon", R.drawable.account)
        )

        // Set up Stories RecyclerView
        val recyclerStories = view.findViewById<RecyclerView>(R.id.storiesRecyclerView)
        recyclerStories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        storyAdapter = StoryAdapter(stories) { story ->
            Toast.makeText(requireContext(), "Clicked on story: ${story.username}", Toast.LENGTH_SHORT).show()
        }
        recyclerStories.adapter = storyAdapter

        // Set up Chats RecyclerView
        val recyclerChats = view.findViewById<RecyclerView>(R.id.chatsRecyclerView)
        recyclerChats.layoutManager = LinearLayoutManager(requireContext())
        chatAdapter = ChatPreviewAdapter(chats) { chat ->
            Toast.makeText(requireContext(), "Clicked on chat with: ${chat.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerChats.adapter = chatAdapter
    }
}
