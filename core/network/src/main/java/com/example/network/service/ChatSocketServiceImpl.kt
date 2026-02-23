package com.example.network.service

import com.example.model.ChatMessage
import com.example.model.ChatRoom
import com.example.model.ConnectionState
import com.example.model.SocketCommand
import com.example.model.SocketEvent
import com.example.network.websocket.WebSocketClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatSocketServiceImpl @Inject constructor(
    private val webSocketClient: WebSocketClient,
    private val json: Json
) : ChatSocketService {

    override val connectionState: StateFlow<ConnectionState> = webSocketClient.connectionState

    override val events: Flow<SocketEvent> = webSocketClient.incomingMessages
        .mapNotNull { raw -> parseEvent(raw) }

    override fun connect() = webSocketClient.connect()

    override fun disconnect() = webSocketClient.disconnect()

    override fun sendMessage(roomId: String, content: String) {
        send(SocketCommand.sendMessage(roomId, content))
    }

    override fun joinRoom(roomId: String) = send(SocketCommand.joinRoom(roomId))

    override fun leaveRoom(roomId: String) = send(SocketCommand.leaveRoom(roomId))

    override fun sendTyping(roomId: String) = send(SocketCommand.typing(roomId))

    override fun sendReadReceipt(roomId: String, messageId: String) {
        send(SocketCommand.readReceipt(roomId, messageId))
    }

    private fun send(command: SocketCommand) {
        val jsonStr = json.encodeToString(SocketCommand.serializer(), command)
        webSocketClient.send(jsonStr)
    }

    private fun parseEvent(raw: String): SocketEvent? {
        return try {
            val response = json.decodeFromString<SocketResponse>(raw)
            response.toSocketEvent()
        } catch (_: Exception) {
            null
        }
    }
}

@Serializable
internal data class SocketResponse(
    val type: String,
    val id: String? = null,
    val roomId: String? = null,
    val senderId: String? = null,
    val senderName: String? = null,
    val senderProfileImageUrl: String? = null,
    val content: String? = null,
    val timestamp: Long? = null,
    val rooms: List<ChatRoomResponse>? = null,
    val userId: String? = null,
    val userName: String? = null,
    val messageId: String? = null,
    val code: Int? = null,
    val message: String? = null
)

@Serializable
internal data class ChatRoomResponse(
    val id: String,
    val name: String,
    val profileImageUrl: String? = null,
    val lastMessage: String? = null,
    val lastMessageTime: String? = null,
    val unreadCount: Int? = null,
    val memberCount: Int? = null
)

internal fun SocketResponse.toSocketEvent(): SocketEvent? {
    return when (type) {
        "MESSAGE" -> {
            val msgId = id ?: return null
            val msgRoomId = roomId ?: return null
            val msgSenderId = senderId ?: return null
            SocketEvent.MessageReceived(
                ChatMessage(
                    id = msgId,
                    roomId = msgRoomId,
                    senderId = msgSenderId,
                    senderName = senderName ?: "Unknown",
                    senderProfileImageUrl = senderProfileImageUrl,
                    content = content ?: "",
                    timestamp = timestamp ?: System.currentTimeMillis(),
                    isMine = false
                )
            )
        }

        "ROOM_LIST" -> SocketEvent.RoomListUpdated(
            rooms?.map {
                ChatRoom(
                    id = it.id,
                    name = it.name,
                    profileImageUrl = it.profileImageUrl,
                    lastMessage = it.lastMessage ?: "",
                    lastMessageTime = it.lastMessageTime ?: "",
                    unreadCount = it.unreadCount ?: 0,
                    memberCount = it.memberCount ?: 1
                )
            } ?: emptyList()
        )

        "ROOM_UPDATE" -> {
            val room = rooms?.firstOrNull() ?: return null
            SocketEvent.RoomUpdated(
                ChatRoom(
                    id = room.id,
                    name = room.name,
                    profileImageUrl = room.profileImageUrl,
                    lastMessage = room.lastMessage ?: "",
                    lastMessageTime = room.lastMessageTime ?: "",
                    unreadCount = room.unreadCount ?: 0,
                    memberCount = room.memberCount ?: 1
                )
            )
        }

        "TYPING" -> {
            val typingRoomId = roomId ?: return null
            val typingUserId = userId ?: return null
            SocketEvent.UserTyping(
                roomId = typingRoomId,
                userId = typingUserId,
                userName = userName ?: "Unknown"
            )
        }

        "READ" -> {
            val readRoomId = roomId ?: return null
            val readMessageId = messageId ?: return null
            val readUserId = userId ?: return null
            SocketEvent.MessageRead(
                roomId = readRoomId,
                messageId = readMessageId,
                userId = readUserId
            )
        }

        "ERROR" -> SocketEvent.Error(
            code = code ?: -1,
            message = message ?: "Unknown error"
        )

        else -> null
    }
}
