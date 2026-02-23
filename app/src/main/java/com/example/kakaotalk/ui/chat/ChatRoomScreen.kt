package com.example.kakaotalk.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ChatMessage
import com.example.kakaotalk.ui.theme.KakaoChatBackground
import com.example.kakaotalk.ui.theme.KakaoMessageBackground
import com.example.kakaotalk.ui.theme.KakaoMessageBackgroundOther
import com.example.kakaotalk.ui.theme.KakaoTextPrimary
import com.example.kakaotalk.ui.theme.KakaoTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    roomName: String,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 메시지가 추가되면 스크롤을 맨 아래로
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KakaoChatBackground)
    ) {
        // 상단 앱바
        TopAppBar(
            title = {
                Text(
                    text = roomName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = KakaoTextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = KakaoTextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // 메시지 목록
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = messages,
                key = { it.id }
            ) { message ->
                ChatMessageItem(message = message)
            }
        }

        // 메시지 입력창
        MessageInputBar(
            messageText = messageText,
            onMessageTextChange = { messageText = it },
            onSendClick = {
                if (messageText.isNotBlank()) {
                    onSendMessage(messageText)
                    messageText = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("a h:mm", Locale.KOREAN)
    val timeString = timeFormat.format(Date(message.timestamp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isMine) {
            // 상대방 프로필 이미지
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(KakaoChatBackground),
                contentAlignment = Alignment.Center
            ) {
                if (message.senderProfileImageUrl != null) {
                    AsyncImage(
                        model = message.senderProfileImageUrl,
                        contentDescription = message.senderName,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = message.senderName.firstOrNull()?.toString() ?: "?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KakaoTextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start
        ) {
            if (!message.isMine) {
                Text(
                    text = message.senderName,
                    fontSize = 12.sp,
                    color = KakaoTextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 메시지 버블
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (message.isMine) 12.dp else 4.dp,
                                bottomEnd = if (message.isMine) 4.dp else 12.dp
                            )
                        )
                        .background(
                            if (message.isMine) KakaoMessageBackground else KakaoMessageBackgroundOther
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = message.content,
                        fontSize = 15.sp,
                        color = KakaoTextPrimary,
                        lineHeight = 20.sp
                    )
                }

                // 시간
                Text(
                    text = timeString,
                    fontSize = 11.sp,
                    color = KakaoTextSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }

        if (message.isMine) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun MessageInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 메시지 입력 필드
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp, max = 120.dp),
                placeholder = {
                    Text(
                        text = "메시지 입력",
                        color = KakaoTextSecondary,
                        fontSize = 14.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = KakaoChatBackground,
                    unfocusedContainerColor = KakaoChatBackground
                ),
                shape = RoundedCornerShape(20.dp),
                maxLines = 4,
                singleLine = false
            )

            // 전송 버튼
            Button(
                onClick = onSendClick,
                enabled = messageText.isNotBlank(),
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (messageText.isNotBlank()) {
                        KakaoMessageBackground
                    } else {
                        KakaoChatBackground
                    },
                    contentColor = if (messageText.isNotBlank()) {
                        KakaoTextPrimary
                    } else {
                        KakaoTextSecondary
                    }
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "전송",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

