package com.example.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_rooms")
data class ChatRoomEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "profile_image_url")
    val profileImageUrl: String? = null,
    @ColumnInfo(name = "last_message")
    val lastMessage: String = "",
    @ColumnInfo(name = "last_message_time")
    val lastMessageTime: String = "",
    @ColumnInfo(name = "last_message_timestamp")
    val lastMessageTimestamp: Long = 0L,
    @ColumnInfo(name = "unread_count")
    val unreadCount: Int = 0,
    @ColumnInfo(name = "member_count")
    val memberCount: Int = 1
)
