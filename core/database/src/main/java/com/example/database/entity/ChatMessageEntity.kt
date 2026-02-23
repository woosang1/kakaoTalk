package com.example.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["room_id"])]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "room_id")
    val roomId: String,
    @ColumnInfo(name = "sender_id")
    val senderId: String,
    @ColumnInfo(name = "sender_name")
    val senderName: String,
    @ColumnInfo(name = "sender_profile_url")
    val senderProfileImageUrl: String? = null,
    val content: String,
    val timestamp: Long,
    @ColumnInfo(name = "is_mine")
    val isMine: Boolean,
    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = true
)
