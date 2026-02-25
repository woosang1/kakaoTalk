package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class SocketCommand(
    val type: String,
    val roomId: String? = null,
    val content: String? = null,
    val userId: String? = null,
    val messageId: String? = null,
    val timestamp: Long? = null
) {
    companion object {
        fun connect(userId: String) = SocketCommand(type = "CONNECT", userId = userId)

        fun sendMessage(roomId: String, content: String) = SocketCommand(
            type = "SEND_MESSAGE",
            roomId = roomId,
            content = content,
            timestamp = System.currentTimeMillis()
        )

        fun joinRoom(roomId: String) = SocketCommand(type = "JOIN_ROOM", roomId = roomId)
        fun leaveRoom(roomId: String) = SocketCommand(type = "LEAVE_ROOM", roomId = roomId)
        fun typing(roomId: String) = SocketCommand(type = "TYPING", roomId = roomId)

        fun readReceipt(roomId: String, messageId: String) = SocketCommand(
            type = "READ",
            roomId = roomId,
            messageId = messageId
        )

        fun ping() = SocketCommand(type = "PING")

        fun gameState(boardData: String) = SocketCommand(type = "GAME_STATE", content = boardData)
        fun gameOver(score: String) = SocketCommand(type = "GAME_OVER", content = score)
        fun gameReady() = SocketCommand(type = "GAME_READY")
        fun drawSyncRequest() = SocketCommand(type = "DRAW_SYNC_REQUEST")
        fun drawSetLimit(count: Int) = SocketCommand(type = "DRAW_SET_LIMIT", content = count.toString())
        fun drawHold(index: Int) = SocketCommand(type = "DRAW_HOLD", content = index.toString())
        fun drawRelease(index: Int) = SocketCommand(type = "DRAW_RELEASE", content = index.toString())
        fun drawPick(index: Int) = SocketCommand(type = "DRAW_PICK", content = index.toString())
        fun drawReset() = SocketCommand(type = "DRAW_RESET")
    }
}
