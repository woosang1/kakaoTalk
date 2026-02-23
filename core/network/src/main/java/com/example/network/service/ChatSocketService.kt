package com.example.network.service

import com.example.model.ConnectionState
import com.example.model.SocketEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatSocketService {
    val connectionState: StateFlow<ConnectionState>
    val events: Flow<SocketEvent>

    fun connect()
    fun disconnect()
    fun sendMessage(roomId: String, content: String)
    fun joinRoom(roomId: String)
    fun leaveRoom(roomId: String)
    fun sendTyping(roomId: String)
    fun sendReadReceipt(roomId: String, messageId: String)
    fun sendGameState(boardData: String)
    fun sendGameOver(score: Int)
}
