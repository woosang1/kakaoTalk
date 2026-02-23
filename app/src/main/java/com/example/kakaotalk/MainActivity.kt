package com.example.kakaotalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kakaotalk.ui.chat.ChatListScreen
import com.example.kakaotalk.ui.chat.ChatRoomScreen
import com.example.kakaotalk.ui.chat.ChatViewModel
import com.example.kakaotalk.ui.theme.KakaoTalkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KakaoTalkTheme {
                ChatApp()
            }
        }
    }
}

@Composable
fun ChatApp(
    viewModel: ChatViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val chatRooms by viewModel.chatRooms.collectAsState()
    val messages by viewModel.currentRoomMessages.collectAsState()
    val currentRoomId by viewModel.currentRoomId.collectAsState()

    val currentRoom = chatRooms.find { it.id == currentRoomId }

    if (currentRoomId != null && currentRoom != null) {
        ChatRoomScreen(
            roomName = currentRoom.name,
            messages = messages,
            onSendMessage = { viewModel.sendMessage(it) },
            onBackClick = { viewModel.goBack() },
            modifier = modifier.fillMaxSize()
        )
    } else {
        ChatListScreen(
            chatRooms = chatRooms,
            onChatRoomClick = { viewModel.selectChatRoom(it) },
            modifier = modifier.fillMaxSize()
        )
    }
}
