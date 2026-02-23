package com.example.kakaotalk.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.model.ConnectionState

private val PIECE_COLORS = arrayOf(
    Color(0xFF1A1A2E),
    Color(0xFF00E5FF),
    Color(0xFFFFEB3B),
    Color(0xFFAB47BC),
    Color(0xFF66BB6A),
    Color(0xFFEF5350),
    Color(0xFF42A5F5),
    Color(0xFFFF9800)
)

private val BG_DARK = Color(0xFF0D0D1A)
private val BG_CARD = Color(0xFF1A1A2E)
private val ACCENT = Color(0xFF00E5FF)
private val GRID_LINE = Color(0xFF2A2A3E)

@Composable
fun TetrisScreen(
    modifier: Modifier = Modifier,
    viewModel: TetrisViewModel = hiltViewModel()
) {
    val myBoard by viewModel.myBoard.collectAsState()
    val myScore by viewModel.myScore.collectAsState()
    val myLevel by viewModel.myLevel.collectAsState()
    val myLines by viewModel.myLines.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()

    val opponentBoard by viewModel.opponentBoard.collectAsState()
    val opponentScore by viewModel.opponentScore.collectAsState()
    val opponentLevel by viewModel.opponentLevel.collectAsState()
    val opponentLines by viewModel.opponentLines.collectAsState()
    val opponentConnected by viewModel.opponentConnected.collectAsState()
    val opponentGameOver by viewModel.opponentGameOver.collectAsState()

    val connState by viewModel.connectionState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BG_DARK)
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        ConnectionStatusBar(state = connState, opponentConnected = opponentConnected)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "TETRIS BATTLE",
            color = ACCENT,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Opponent board
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val opponentLabelColor by animateColorAsState(
                    targetValue = if (opponentConnected) Color(0xFFFF5252) else Color.Gray,
                    label = "opponentLabel"
                )
                Text(
                    text = if (opponentConnected) "OPPONENT" else "WAITING...",
                    color = opponentLabelColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(3.dp))

                val borderColor by animateColorAsState(
                    targetValue = if (opponentConnected) Color(0xFFFF5252).copy(alpha = 0.5f) else Color(0xFF333355),
                    label = "opponentBorder"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.5f)
                        .border(1.dp, borderColor, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    TetrisBoardView(board = opponentBoard)

                    if (!opponentConnected) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "상대방\n대기 중",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    if (opponentGameOver && opponentConnected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "GAME\nOVER",
                                color = Color(0xFFFF5252),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ScoreLabel(label = "SCR", value = opponentScore)
                    ScoreLabel(label = "LV", value = opponentLevel)
                    ScoreLabel(label = "LN", value = opponentLines)
                }
            }

            // My board
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YOU",
                    color = ACCENT,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(3.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.5f)
                        .border(1.dp, ACCENT.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    TetrisBoardView(board = myBoard)

                    if (isGameOver) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "GAME OVER",
                                    color = ACCENT,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Score: $myScore",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ScoreLabel(label = "SCORE", value = myScore)
                    ScoreLabel(label = "LEVEL", value = myLevel)
                    ScoreLabel(label = "LINES", value = myLines)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isPlaying) {
            GameControls(
                onLeft = { viewModel.moveLeft() },
                onRight = { viewModel.moveRight() },
                onRotate = { viewModel.rotatePiece() },
                onDown = { viewModel.moveDown() },
                onHardDrop = { viewModel.hardDrop() }
            )
        } else {
            Button(
                onClick = { viewModel.startGame() },
                colors = ButtonDefaults.buttonColors(containerColor = ACCENT),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp)
            ) {
                Text(
                    text = if (isGameOver) "다시 시작" else "게임 시작",
                    color = BG_DARK,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun ConnectionStatusBar(
    state: ConnectionState,
    opponentConnected: Boolean
) {
    val (statusText, dotColor) = when {
        state == ConnectionState.CONNECTED && opponentConnected ->
            "서버 연결됨 · 상대방 접속 중" to Color(0xFF66BB6A)
        state == ConnectionState.CONNECTED ->
            "서버 연결됨 · 상대방 대기 중" to Color(0xFFFFA726)
        state == ConnectionState.CONNECTING || state == ConnectionState.RECONNECTING ->
            "서버 연결 중..." to Color(0xFFFFA726)
        else ->
            "서버 연결 안됨" to Color(0xFFEF5350)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BG_CARD, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = dotColor)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = statusText,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun TetrisBoardView(
    board: Array<IntArray>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val cellW = size.width / TetrisEngine.COLS
        val cellH = size.height / TetrisEngine.ROWS
        val cornerR = CornerRadius(2f, 2f)

        for (row in 0 until TetrisEngine.ROWS) {
            for (col in 0 until TetrisEngine.COLS) {
                drawRect(
                    color = GRID_LINE,
                    topLeft = Offset(col * cellW, row * cellH),
                    size = Size(cellW - 0.5f, cellH - 0.5f)
                )
            }
        }

        for (row in board.indices) {
            for (col in board[row].indices) {
                val value = board[row][col]
                if (value in 1..7) {
                    val x = col * cellW
                    val y = row * cellH
                    val color = PIECE_COLORS[value]

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x + 0.5f, y + 0.5f),
                        size = Size(cellW - 1f, cellH - 1f),
                        cornerRadius = cornerR
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.25f),
                        topLeft = Offset(x + 0.5f, y + 0.5f),
                        size = Size(cellW - 1f, (cellH - 1f) * 0.35f),
                        cornerRadius = cornerR
                    )
                }
            }
        }
    }
}

@Composable
private fun GameControls(
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onRotate: () -> Unit,
    onDown: () -> Unit,
    onHardDrop: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControlButton(text = "◀", onClick = onLeft)
        ControlButton(text = "▶", onClick = onRight)
        Spacer(modifier = Modifier.width(12.dp))
        ControlButton(text = "↻", onClick = onRotate, accent = true)
        Spacer(modifier = Modifier.width(12.dp))
        ControlButton(text = "▼", onClick = onDown)
        ControlButton(text = "⬇", onClick = onHardDrop, accent = true)
    }
}

@Composable
private fun ControlButton(
    text: String,
    onClick: () -> Unit,
    accent: Boolean = false
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (accent) ACCENT.copy(alpha = 0.2f) else BG_CARD
        ),
        shape = CircleShape,
        modifier = Modifier.size(52.dp),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Text(
            text = text,
            color = if (accent) ACCENT else Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ScoreLabel(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 9.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = value.toString(),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
