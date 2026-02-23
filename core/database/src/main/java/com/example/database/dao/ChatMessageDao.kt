package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_messages WHERE room_id = :roomId ORDER BY timestamp ASC")
    fun getMessagesByRoom(roomId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET is_sent = 1 WHERE id = :messageId")
    suspend fun markAsSent(messageId: String)

    @Query("SELECT * FROM chat_messages WHERE is_sent = 0")
    suspend fun getUnsentMessages(): List<ChatMessageEntity>

    @Query("DELETE FROM chat_messages WHERE room_id = :roomId")
    suspend fun deleteByRoom(roomId: String)

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getCount(): Int
}
