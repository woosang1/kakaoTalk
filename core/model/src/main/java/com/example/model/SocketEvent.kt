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
}
