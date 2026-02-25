package com.example.model

sealed interface SocketEvent {
    data class MessageReceived(val message: ChatMessage) : SocketEvent
    data class RoomListUpdated(val rooms: List<ChatRoom>) : SocketEvent
    data class RoomUpdated(val room: ChatRoom) : SocketEvent
    data class UserTyping(val roomId: String, val userId: String, val userName: String) : SocketEvent
    data class MessageRead(val roomId: String, val messageId: String, val userId: String) : SocketEvent
    data class Error(val code: Int, val message: String) : SocketEvent
    data class GameStateReceived(val boardData: String) : SocketEvent
    data class OpponentGameOver(val opponentScore: Int) : SocketEvent
    data object OpponentReady : SocketEvent
    data class PlayerCountUpdated(val count: Int) : SocketEvent
    data class DrawSyncReceived(val boardData: String) : SocketEvent
    data class DrawPicked(val index: Int, val rank: Int, val byUser: String?) : SocketEvent
    data class DrawHeld(val index: Int, val byUser: String?) : SocketEvent
    data class DrawReleased(val index: Int) : SocketEvent
    data class DrawLimitUpdated(val total: Int, val remaining: Int) : SocketEvent
    data object DrawReseted : SocketEvent
    data class DrawRejected(val reason: String) : SocketEvent
}
