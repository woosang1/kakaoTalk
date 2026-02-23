package com.example.kakaotalk.data.repository

import com.example.database.dao.ChatMessageDao
import com.example.database.dao.ChatRoomDao
import com.example.database.entity.ChatMessageEntity
import com.example.database.entity.ChatRoomEntity
import com.example.model.ChatMessage
import com.example.model.ChatRoom
import com.example.model.ConnectionState
import com.example.model.SocketEvent
import com.example.network.service.ChatSocketService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 네트워크(소켓) + 로컬(Room)을 조율하는 단일 진입점.
 *
 * - Optimistic Update : 메시지 전송 시 로컬 저장 → 소켓 전송 → 전송 확인 마킹
 * - Offline Resilience : 미전송 메시지 재전송 큐
 * - Reactive Streams  : Room Flow → UI 자동 갱신
 */
@Singleton
class ChatRepository @Inject constructor(
    private val chatSocketService: ChatSocketService,
    private val chatMessageDao: ChatMessageDao,
    private val chatRoomDao: ChatRoomDao
) {
    val connectionState: StateFlow<ConnectionState> = chatSocketService.connectionState

    fun getChatRooms(): Flow<List<ChatRoom>> =
        chatRoomDao.getAllRooms().map { entities -> entities.map { it.toDomain() } }

    fun getMessages(roomId: String): Flow<List<ChatMessage>> =
        chatMessageDao.getMessagesByRoom(roomId).map { entities -> entities.map { it.toDomain() } }

    fun connect() = chatSocketService.connect()

    fun disconnect() = chatSocketService.disconnect()

    suspend fun sendMessage(roomId: String, content: String) {
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val timeStr = formatTime(timestamp)

        val entity = ChatMessageEntity(
            id = messageId,
            roomId = roomId,
            senderId = "me",
            senderName = "나",
            content = content,
            timestamp = timestamp,
            isMine = true,
            isSent = false
        )
        chatMessageDao.insert(entity)
        chatRoomDao.updateLastMessage(roomId, content, timeStr, timestamp)

        chatSocketService.sendMessage(roomId, content)
        chatMessageDao.markAsSent(messageId)
    }

    suspend fun observeSocketEvents() {
        chatSocketService.events.collect { event ->
            when (event) {
                is SocketEvent.MessageReceived -> handleIncomingMessage(event.message)
                is SocketEvent.RoomListUpdated -> handleRoomListUpdate(event.rooms)
                is SocketEvent.RoomUpdated -> handleRoomUpdate(event.room)
                else -> Unit
            }
        }
    }

    suspend fun resendUnsentMessages() {
        val unsent = chatMessageDao.getUnsentMessages()
        unsent.forEach { msg ->
            chatSocketService.sendMessage(msg.roomId, msg.content)
            chatMessageDao.markAsSent(msg.id)
        }
    }

    suspend fun initializeSampleData() {
        if (chatRoomDao.getCount() > 0) return

        val now = System.currentTimeMillis()
        val rooms = listOf(
            ChatRoomEntity(
                id = "1", name = "김철수",
                lastMessage = "안녕하세요!", lastMessageTime = "오후 2:30",
                lastMessageTimestamp = now - 3_600_000, unreadCount = 2
            ),
            ChatRoomEntity(
                id = "2", name = "이영희",
                lastMessage = "오늘 날씨가 좋네요", lastMessageTime = "오전 11:20",
                lastMessageTimestamp = now - 7_200_000
            ),
            ChatRoomEntity(
                id = "3", name = "가족 단체방",
                lastMessage = "저녁 뭐 먹을까요?", lastMessageTime = "어제",
                lastMessageTimestamp = now - 86_400_000, unreadCount = 5, memberCount = 4
            ),
            ChatRoomEntity(
                id = "4", name = "박지민",
                lastMessage = "회의 일정 확인 부탁드립니다", lastMessageTime = "월요일",
                lastMessageTimestamp = now - 172_800_000
            ),
            ChatRoomEntity(
                id = "5", name = "최수영",
                lastMessage = "주말에 만나요!", lastMessageTime = "토요일",
                lastMessageTimestamp = now - 259_200_000, unreadCount = 1
            )
        )
        chatRoomDao.insertAll(rooms)

        val messages = listOf(
            ChatMessageEntity(
                id = "m1", roomId = "1", senderId = "user1", senderName = "김철수",
                content = "안녕하세요!", timestamp = now - 3_600_000, isMine = false
            ),
            ChatMessageEntity(
                id = "m2", roomId = "1", senderId = "me", senderName = "나",
                content = "네, 안녕하세요!", timestamp = now - 3_300_000, isMine = true
            ),
            ChatMessageEntity(
                id = "m3", roomId = "1", senderId = "user1", senderName = "김철수",
                content = "오늘 날씨가 정말 좋네요. 산책하러 가실래요?",
                timestamp = now - 3_000_000, isMine = false
            ),
            ChatMessageEntity(
                id = "m4", roomId = "1", senderId = "me", senderName = "나",
                content = "좋은 생각이에요! 어디로 갈까요?",
                timestamp = now - 2_700_000, isMine = true
            ),
            ChatMessageEntity(
                id = "m5", roomId = "2", senderId = "user2", senderName = "이영희",
                content = "오늘 날씨가 좋네요", timestamp = now - 7_200_000, isMine = false
            ),
            ChatMessageEntity(
                id = "m6", roomId = "2", senderId = "me", senderName = "나",
                content = "맞아요! 정말 좋은 날씨예요",
                timestamp = now - 6_900_000, isMine = true
            ),
            ChatMessageEntity(
                id = "m7", roomId = "3", senderId = "user3", senderName = "엄마",
                content = "저녁 뭐 먹을까요?", timestamp = now - 86_400_000, isMine = false
            ),
            ChatMessageEntity(
                id = "m8", roomId = "3", senderId = "me", senderName = "나",
                content = "치킨은 어떠세요?", timestamp = now - 86_100_000, isMine = true
            )
        )
        chatMessageDao.insertAll(messages)
    }

    private suspend fun handleIncomingMessage(message: ChatMessage) {
        chatMessageDao.insert(message.toEntity())
        val timeStr = formatTime(message.timestamp)
        chatRoomDao.updateLastMessage(message.roomId, message.content, timeStr, message.timestamp)
    }

    private suspend fun handleRoomListUpdate(rooms: List<ChatRoom>) {
        chatRoomDao.insertAll(rooms.map { it.toEntity() })
    }

    private suspend fun handleRoomUpdate(room: ChatRoom) {
        chatRoomDao.insert(room.toEntity())
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("a h:mm", Locale.KOREA)
        return sdf.format(Date(timestamp))
    }
}

private fun ChatMessageEntity.toDomain() = ChatMessage(
    id = id, roomId = roomId, senderId = senderId,
    senderName = senderName, senderProfileImageUrl = senderProfileImageUrl,
    content = content, timestamp = timestamp, isMine = isMine
)

private fun ChatRoomEntity.toDomain() = ChatRoom(
    id = id, name = name, profileImageUrl = profileImageUrl,
    lastMessage = lastMessage, lastMessageTime = lastMessageTime,
    unreadCount = unreadCount, memberCount = memberCount
)

private fun ChatMessage.toEntity() = ChatMessageEntity(
    id = id, roomId = roomId, senderId = senderId,
    senderName = senderName, senderProfileImageUrl = senderProfileImageUrl,
    content = content, timestamp = timestamp, isMine = isMine
)

private fun ChatRoom.toEntity() = ChatRoomEntity(
    id = id, name = name, profileImageUrl = profileImageUrl,
    lastMessage = lastMessage, lastMessageTime = lastMessageTime,
    unreadCount = unreadCount, memberCount = memberCount
)
