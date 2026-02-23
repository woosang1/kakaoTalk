package com.example.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.database.dao.ChatMessageDao
import com.example.database.dao.ChatRoomDao
import com.example.database.entity.ChatMessageEntity
import com.example.database.entity.ChatRoomEntity

@Database(
    entities = [
        ChatMessageEntity::class,
        ChatRoomEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun chatRoomDao(): ChatRoomDao
}
