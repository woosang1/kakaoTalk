package com.example.network.service

import android.util.Log
import com.example.model.ChatMessage
import com.example.model.ChatRoom
import com.example.model.ConnectionState
import com.example.model.SocketCommand
import com.example.model.SocketEvent
import com.example.network.websocket.WebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatSocketServiceImpl @Inject constructor(
    private val webSocketClient: WebSocketClient,
    private val json: Json
) : ChatSocketService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _chatEvents = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 256)
    private val _gameEvents = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 64, replay = 5)
    private val _gachaEvents = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 64, replay = 10)

    override val connectionState: StateFlow<ConnectionState> = webSocketClient.connectionState
    override val events: Flow<SocketEvent> = _chatEvents.asSharedFlow()
    override val gameEvents: Flow<SocketEvent> = _gameEvents.asSharedFlow()
    override val gachaEvents: Flow<SocketEvent> = _gachaEvents.asSharedFlow()

    init {
        scope.launch {
            webSocketClient.incomingMessages.collect { raw ->
                val event = parseEvent(raw) ?: return@collect
                when (event) {
                    is SocketEvent.GameStateReceived,
                    is SocketEvent.OpponentGameOver,
                    is SocketEvent.OpponentReady,
                    is SocketEvent.PlayerCountUpdated -> {
                        Log.d(TAG, "→ gameEvents: ${event::class.simpleName}")
                        _gameEvents.emit(event)
                    }
                    else -> {}
                }
                when (event) {
                    is SocketEvent.DrawSyncReceived,
                    is SocketEvent.DrawPicked,
                    is SocketEvent.DrawHeld,
                    is SocketEvent.DrawReleased,
                    is SocketEvent.DrawLimitUpdated,
                    is SocketEvent.DrawReseted,
                    is SocketEvent.DrawRejected -> {
                        Log.d(TAG, "→ gachaEvents: ${event::class.simpleName}")
                        _gachaEvents.emit(event)
                    }
                    else -> {}
                }
                _chatEvents.emit(event)
            }
        }
    }

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

    override fun sendGameState(boardData: String) {
        send(SocketCommand.gameState(boardData))
    }

    override fun sendGameOver(score: Int) {
        send(SocketCommand.gameOver(score.toString()))
    }

    override fun sendGameReady() {
        send(SocketCommand.gameReady())
    }

    override fun sendDrawSyncRequest() {
        send(SocketCommand.drawSyncRequest())
    }

    override fun sendDrawSetLimit(count: Int) {
        send(SocketCommand.drawSetLimit(count))
    }

    override fun sendDrawHold(index: Int) {
        send(SocketCommand.drawHold(index))
    }

    override fun sendDrawRelease(index: Int) {
        send(SocketCommand.drawRelease(index))
    }

    override fun sendDrawPick(index: Int) {
        send(SocketCommand.drawPick(index))
    }

    override fun sendDrawReset() {
        send(SocketCommand.drawReset())
    }

    private fun send(command: SocketCommand) {
        val jsonStr = json.encodeToString(SocketCommand.serializer(), command)
        webSocketClient.send(jsonStr)
    }

    private fun parseEvent(raw: String): SocketEvent? {
        return try {
            val response = json.decodeFromString<SocketResponse>(raw)
            val event = response.toSocketEvent()
            if (event == null) {
                Log.w(TAG, "toSocketEvent null for type=${response.type}")
            } else {
                Log.d(TAG, "⬇ RECV type=${response.type} → ${event::class.simpleName}")
            }
            event
        } catch (e: Exception) {
            Log.e(TAG, "parseEvent FAIL: ${e.message}, raw=${raw.take(200)}")
            null
        }
    }

    companion object {
        private const val TAG = "ChatSocket"
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

        "GAME_STATE" -> SocketEvent.GameStateReceived(boardData = content ?: return null)
        "GAME_OVER" -> SocketEvent.OpponentGameOver(opponentScore = content?.toIntOrNull() ?: 0)
        "GAME_READY" -> SocketEvent.OpponentReady
        "PLAYER_COUNT" -> SocketEvent.PlayerCountUpdated(count = content?.toIntOrNull() ?: 0)
        "DRAW_SYNC" -> SocketEvent.DrawSyncReceived(boardData = content ?: "")
        "DRAW_PICKED" -> {
            val parts = content?.split("|") ?: return null
            if (parts.size < 3) return null
            SocketEvent.DrawPicked(
                index = parts[0].toIntOrNull() ?: return null,
                rank = parts[1].toIntOrNull() ?: 0,
                byUser = parts[2]
            )
        }
        "DRAW_HELD" -> {
            val parts = content?.split("|") ?: return null
            if (parts.size < 2) return null
            SocketEvent.DrawHeld(
                index = parts[0].toIntOrNull() ?: return null,
                byUser = parts[1]
            )
        }
        "DRAW_RELEASED" -> SocketEvent.DrawReleased(index = content?.toIntOrNull() ?: return null)
        "DRAW_LIMIT" -> {
            val parts = content?.split("|") ?: return null
            if (parts.size < 2) return null
            SocketEvent.DrawLimitUpdated(
                total = parts[0].toIntOrNull() ?: 0,
                remaining = parts[1].toIntOrNull() ?: 0
            )
        }
        "DRAW_RESETED" -> SocketEvent.DrawReseted
        "DRAW_REJECT" -> SocketEvent.DrawRejected(reason = content ?: "이미 뽑힌 칸입니다.")

        else -> null
    }
}
