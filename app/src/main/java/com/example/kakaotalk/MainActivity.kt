package com.example.kakaotalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.kakaotalk.ui.chat.ChatListScreen
import com.example.kakaotalk.ui.chat.ChatRoomScreen
import com.example.kakaotalk.ui.chat.ChatViewModel
import com.example.kakaotalk.ui.theme.KakaoTalkTheme

class MainActivity : ComponentActivity() {
    private val viewModel = ChatViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KakaoTalkTheme {
                ChatApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ChatApp(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val chatRooms by viewModel.chatRooms.collectAsState()
    val messages by viewModel.currentRoomMessages.collectAsState()
    val currentRoomId by viewModel.currentRoomId.collectAsState()

    val currentRoom = chatRooms.find { it.id == currentRoomId }

    if (currentRoomId != null && currentRoom != null) {
        // 채팅방 화면
        ChatRoomScreen(
            roomName = currentRoom.name,
            messages = messages,
            onSendMessage = { message ->
                viewModel.sendMessage(message)
            },
            onBackClick = {
                viewModel.goBack()
            },
            modifier = modifier.fillMaxSize()
        )
    } else {
        // 채팅 목록 화면
        ChatListScreen(
            chatRooms = chatRooms,
            onChatRoomClick = { roomId ->
                viewModel.selectChatRoom(roomId)
            },
            modifier = modifier.fillMaxSize()
        )
    }
}