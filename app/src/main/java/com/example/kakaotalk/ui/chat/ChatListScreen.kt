package com.example.kakaotalk.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kakaotalk.model.ChatRoom
import com.example.kakaotalk.ui.theme.KakaoChatBackground
import com.example.kakaotalk.ui.theme.KakaoDivider
import com.example.kakaotalk.ui.theme.KakaoTextPrimary
import com.example.kakaotalk.ui.theme.KakaoTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chatRooms: List<ChatRoom>,
    onChatRoomClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KakaoChatBackground)
    ) {
        // 상단 앱바
        TopAppBar(
            title = {
                Text(
                    text = "카카오톡",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = KakaoTextPrimary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // 채팅방 목록
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = chatRooms,
                key = { it.id }
            ) { chatRoom ->
                ChatRoomItem(
                    chatRoom = chatRoom,
                    onClick = { onChatRoomClick(chatRoom.id) }
                )
                HorizontalDivider(
                    color = KakaoDivider,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 80.dp)
                )
            }
        }
    }
}

@Composable
fun ChatRoomItem(
    chatRoom: ChatRoom,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(KakaoChatBackground),
            contentAlignment = Alignment.Center
        ) {
            if (chatRoom.profileImageUrl != null) {
                AsyncImage(
                    model = chatRoom.profileImageUrl,
                    contentDescription = chatRoom.name,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 기본 프로필 아이콘
                Text(
                    text = chatRoom.name.firstOrNull()?.toString() ?: "?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = KakaoTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 채팅방 정보
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chatRoom.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = KakaoTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = chatRoom.lastMessageTime,
                    fontSize = 12.sp,
                    color = KakaoTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chatRoom.lastMessage,
                    fontSize = 14.sp,
                    color = KakaoTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (chatRoom.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFFF4444))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = chatRoom.unreadCount.toString(),
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

