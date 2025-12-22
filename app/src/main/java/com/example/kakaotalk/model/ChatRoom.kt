package com.example.kakaotalk.model

data class ChatRoom(
    val id: String,
    val name: String,
    val profileImageUrl: String? = null,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0
)

data class ChatMessage(
    val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val senderProfileImageUrl: String? = null,
    val content: String,
    val timestamp: Long,
    val isMine: Boolean
)

