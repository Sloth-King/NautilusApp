package com.example.nautilusapp

data class ChatMessageData(
    val message: String,
    val isSentByMe: Boolean,
    val isFriendRequest: Boolean = false
)
