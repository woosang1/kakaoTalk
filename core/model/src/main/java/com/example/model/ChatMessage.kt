package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val senderProfileImageUrl: String? = null,
    val content: String,
    val timestamp: Long,
    val isMine: Boolean = false
)
