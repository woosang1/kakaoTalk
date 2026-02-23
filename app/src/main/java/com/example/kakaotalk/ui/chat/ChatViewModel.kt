package com.example.kakaotalk.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kakaotalk.data.repository.ChatRepository
import com.example.model.ChatMessage
import com.example.model.ChatRoom
import com.example.model.ConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    val chatRooms: StateFlow<List<ChatRoom>> = chatRepository.getChatRooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val connectionState: StateFlow<ConnectionState> = chatRepository.connectionState

    private val _currentRoomId = MutableStateFlow<String?>(null)
    val currentRoomId: StateFlow<String?> = _currentRoomId.asStateFlow()

    val currentRoomMessages: StateFlow<List<ChatMessage>> = _currentRoomId
        .flatMapLatest { roomId ->
            if (roomId != null) chatRepository.getMessages(roomId)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { chatRepository.initializeSampleData() }

        chatRepository.connect()

        viewModelScope.launch { chatRepository.observeSocketEvents() }

        viewModelScope.launch {
            chatRepository.connectionState.collect { state ->
                if (state == ConnectionState.CONNECTED) {
                    chatRepository.resendUnsentMessages()
                }
            }
        }
    }

    fun selectChatRoom(roomId: String) {
        _currentRoomId.value = roomId
    }

    fun sendMessage(content: String) {
        val roomId = _currentRoomId.value ?: return
        viewModelScope.launch {
            chatRepository.sendMessage(roomId, content)
        }
    }

    fun goBack() {
        _currentRoomId.value = null
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.disconnect()
    }
}
