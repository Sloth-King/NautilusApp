package com.example.nautilusapp

class ChatPreviewData(
    val chatName: String,       // Name
    val lastMessage: String,    // last msg
    val time: String,       // time of last message (optional i guess)
    val profileImageResId: Int, // Photo
    val id: Int, //id of the discussion in db
    val isFriendRequest: Boolean
)