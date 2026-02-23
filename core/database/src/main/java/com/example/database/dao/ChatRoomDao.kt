package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.entity.ChatRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatRoomDao {

    @Query("SELECT * FROM chat_rooms ORDER BY last_message_timestamp DESC")
    fun getAllRooms(): Flow<List<ChatRoomEntity>>

    @Query("SELECT * FROM chat_rooms WHERE id = :roomId")
    suspend fun getRoomById(roomId: String): ChatRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(room: ChatRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rooms: List<ChatRoomEntity>)

    @Query(
        """
        UPDATE chat_rooms 
        SET last_message = :lastMessage,
            last_message_time = :lastMessageTime,
            last_message_timestamp = :timestamp 
        WHERE id = :roomId
        """
    )
    suspend fun updateLastMessage(
        roomId: String,
        lastMessage: String,
        lastMessageTime: String,
        timestamp: Long
    )

    @Query("UPDATE chat_rooms SET unread_count = :count WHERE id = :roomId")
    suspend fun updateUnreadCount(roomId: String, count: Int)

    @Query("SELECT COUNT(*) FROM chat_rooms")
    suspend fun getCount(): Int
}
