package com.example.kakaotalk.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.kakaotalk.model.ChatMessage
import com.example.kakaotalk.model.ChatRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel {
    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms.asStateFlow()

    private val _currentRoomMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentRoomMessages: StateFlow<List<ChatMessage>> = _currentRoomMessages.asStateFlow()

    private val _currentRoomId = MutableStateFlow<String?>(null)
    val currentRoomId: StateFlow<String?> = _currentRoomId.asStateFlow()

    init {
        // 샘플 데이터 초기화
        loadSampleData()
    }

    private fun loadSampleData() {
        _chatRooms.value = listOf(
            ChatRoom(
                id = "1",
                name = "친구 1",
                lastMessage = "안녕하세요!",
                lastMessageTime = "오후 2:30",
                unreadCount = 2
            ),
            ChatRoom(
                id = "2",
                name = "친구 2",
                lastMessage = "오늘 날씨가 좋네요",
                lastMessageTime = "오전 11:20",
                unreadCount = 0
            ),
            ChatRoom(
                id = "3",
                name = "가족 단체방",
                lastMessage = "저녁 뭐 먹을까요?",
                lastMessageTime = "어제",
                unreadCount = 5
            ),
            ChatRoom(
                id = "4",
                name = "동료 1",
                lastMessage = "회의 일정 확인 부탁드립니다",
                lastMessageTime = "월요일",
                unreadCount = 0
            ),
            ChatRoom(
                id = "5",
                name = "친구 3",
                lastMessage = "주말에 만나요!",
                lastMessageTime = "토요일",
                unreadCount = 1
            )
        )
    }

    fun selectChatRoom(roomId: String) {
        _currentRoomId.value = roomId
        // 실제로는 해당 채팅방의 메시지를 로드해야 함
        // 여기서는 샘플 데이터를 사용
        _currentRoomMessages.value = when (roomId) {
            "1" -> listOf(
                ChatMessage(
                    id = "m1",
                    roomId = "1",
                    senderId = "other",
                    senderName = "친구 1",
                    content = "안녕하세요!",
                    timestamp = System.currentTimeMillis() - 3600000,
                    isMine = false
                ),
                ChatMessage(
                    id = "m2",
                    roomId = "1",
                    senderId = "me",
                    senderName = "나",
                    content = "네, 안녕하세요!",
                    timestamp = System.currentTimeMillis() - 3300000,
                    isMine = true
                ),
                ChatMessage(
                    id = "m3",
                    roomId = "1",
                    senderId = "other",
                    senderName = "친구 1",
                    content = "오늘 날씨가 정말 좋네요. 산책하러 가실래요?",
                    timestamp = System.currentTimeMillis() - 3000000,
                    isMine = false
                ),
                ChatMessage(
                    id = "m4",
                    roomId = "1",
                    senderId = "me",
                    senderName = "나",
                    content = "좋은 생각이에요! 어디로 갈까요?",
                    timestamp = System.currentTimeMillis() - 2700000,
                    isMine = true
                )
            )
            "2" -> listOf(
                ChatMessage(
                    id = "m5",
                    roomId = "2",
                    senderId = "other",
                    senderName = "친구 2",
                    content = "오늘 날씨가 좋네요",
                    timestamp = System.currentTimeMillis() - 7200000,
                    isMine = false
                ),
                ChatMessage(
                    id = "m6",
                    roomId = "2",
                    senderId = "me",
                    senderName = "나",
                    content = "맞아요! 정말 좋은 날씨예요",
                    timestamp = System.currentTimeMillis() - 6900000,
                    isMine = true
                )
            )
            "3" -> listOf(
                ChatMessage(
                    id = "m7",
                    roomId = "3",
                    senderId = "other",
                    senderName = "엄마",
                    content = "저녁 뭐 먹을까요?",
                    timestamp = System.currentTimeMillis() - 86400000,
                    isMine = false
                ),
                ChatMessage(
                    id = "m8",
                    roomId = "3",
                    senderId = "me",
                    senderName = "나",
                    content = "치킨은 어떠세요?",
                    timestamp = System.currentTimeMillis() - 86100000,
                    isMine = true
                )
            )
            else -> emptyList()
        }
    }

    fun sendMessage(content: String) {
        val roomId = _currentRoomId.value ?: return

        val newMessage = ChatMessage(
            id = "m${System.currentTimeMillis()}",
            roomId = roomId,
            senderId = "me",
            senderName = "나",
            content = content,
            timestamp = System.currentTimeMillis(),
            isMine = true
        )

        _currentRoomMessages.value = _currentRoomMessages.value + newMessage

        // 채팅방 목록의 마지막 메시지 업데이트
        _chatRooms.value = _chatRooms.value.map { room ->
            if (room.id == roomId) {
                room.copy(
                    lastMessage = content,
                    lastMessageTime = "지금"
                )
            } else {
                room
            }
        }
    }

    fun goBack() {
        _currentRoomId.value = null
    }
}

