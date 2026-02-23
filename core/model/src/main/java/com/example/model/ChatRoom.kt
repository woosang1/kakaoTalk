package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatRoom(
    val id: String,
    val name: String,
    val profileImageUrl: String? = null,
    val lastMessage: String = "",
    val lastMessageTime: String = "",
    val unreadCount: Int = 0,
    val memberCount: Int = 1
)
