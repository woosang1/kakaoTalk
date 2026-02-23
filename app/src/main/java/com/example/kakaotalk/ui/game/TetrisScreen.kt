package com.example.kakaotalk.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.model.ConnectionState
import kotlinx.coroutines.delay

private val PIECE_COLORS = arrayOf(
    Color(0xFF1A1A2E), Color(0xFF00E5FF), Color(0xFFFFEB3B),
    Color(0xFFAB47BC), Color(0xFF66BB6A), Color(0xFFEF5350),
    Color(0xFF42A5F5), Color(0xFFFF9800)
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
    val gameMode by viewModel.gameMode.collectAsState()
    when (gameMode) {
        null -> ModeSelectContent(
            onSingle = { viewModel.selectMode(GameMode.SINGLE) },
            onMulti = { viewModel.selectMode(GameMode.MULTI) },
            modifier = modifier
        )
        GameMode.SINGLE -> SinglePlayerContent(viewModel, modifier)
        GameMode.MULTI -> MultiPlayerContent(viewModel, modifier)
    }
}

// ─── Mode Select ─────────────────────────────────────

@Composable
private fun ModeSelectContent(
    onSingle: () -> Unit,
    onMulti: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BG_DARK),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("TETRIS", color = ACCENT, fontSize = 48.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp)
        Spacer(Modifier.height(8.dp))
        Text("BATTLE", color = Color.White.copy(alpha = 0.5f), fontSize = 18.sp, letterSpacing = 12.sp)
        Spacer(Modifier.height(56.dp))

        ModeCard(
            title = "싱글 플레이",
            subtitle = "혼자 즐기기",
            color = ACCENT,
            onClick = onSingle
        )
        Spacer(Modifier.height(16.dp))
        ModeCard(
            title = "멀티 플레이",
            subtitle = "친구와 실시간 대전",
            color = Color(0xFFFF5252),
            onClick = onMulti
        )
    }
}

@Composable
private fun ModeCard(title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .height(72.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(subtitle, color = color.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}

// ─── Single Player ───────────────────────────────────

@Composable
private fun SinglePlayerContent(vm: TetrisViewModel, modifier: Modifier) {
    val myBoard by vm.myBoard.collectAsState()
    val myScore by vm.myScore.collectAsState()
    val myLevel by vm.myLevel.collectAsState()
    val myLines by vm.myLines.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val isGameOver by vm.isGameOver.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BG_DARK)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        TopBar(title = "TETRIS", onBack = { vm.backToModeSelect() })
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(0.75f)
                .aspectRatio(0.5f)
                .align(Alignment.CenterHorizontally)
                .border(1.dp, ACCENT.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            TetrisBoardView(board = myBoard)
            if (isGameOver) GameOverOverlay(myScore)
        }

        Spacer(Modifier.height(6.dp))
        StatsRow(myScore, myLevel, myLines)
        Spacer(Modifier.height(8.dp))

        if (isPlaying) {
            GameControls(vm)
        } else {
            ActionButton(
                text = if (isGameOver) "다시 시작" else "게임 시작",
                onClick = { vm.startSingleGame() }
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ─── Multi Player ────────────────────────────────────

@Composable
private fun MultiPlayerContent(vm: TetrisViewModel, modifier: Modifier) {
    val myBoard by vm.myBoard.collectAsState()
    val myScore by vm.myScore.collectAsState()
    val myLevel by vm.myLevel.collectAsState()
    val myLines by vm.myLines.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val isGameOver by vm.isGameOver.collectAsState()
    val opponentBoard by vm.opponentBoard.collectAsState()
    val opponentScore by vm.opponentScore.collectAsState()
    val opponentLevel by vm.opponentLevel.collectAsState()
    val opponentLines by vm.opponentLines.collectAsState()
    val opponentConnected by vm.opponentConnected.collectAsState()
    val opponentReady by vm.opponentReady.collectAsState()
    val opponentGameOver by vm.opponentGameOver.collectAsState()
    val myReady by vm.myReady.collectAsState()
    val countdown by vm.countdown.collectAsState()
    val connState by vm.connectionState.collectAsState()
    val playerCount by vm.playerCount.collectAsState()
    val debugInfo by vm.debugInfo.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BG_DARK)
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(6.dp))
        TopBar(title = "TETRIS BATTLE", onBack = if (!isPlaying) {{ vm.backToModeSelect() }} else null)
        Spacer(Modifier.height(2.dp))
        ConnectionStatusBar(connState, opponentConnected, playerCount)
        Text(debugInfo, color = Color.White.copy(0.3f), fontSize = 8.sp)
        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Opponent (small)
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val labelColor by animateColorAsState(
                    if (opponentConnected) Color(0xFFFF5252) else Color.Gray, label = "ol"
                )
                Text(
                    if (opponentConnected) "OPPONENT" else "WAITING",
                    color = labelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp
                )
                Spacer(Modifier.height(3.dp))
                val borderC by animateColorAsState(
                    if (opponentConnected) Color(0xFFFF5252).copy(0.5f) else Color(0xFF333355), label = "ob"
                )
                Box(
                    Modifier.weight(1f).aspectRatio(0.5f).border(1.dp, borderC, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    TetrisBoardView(board = opponentBoard)
                    if (!opponentConnected) {
                        Text("상대방\n대기 중", color = Color.White.copy(0.3f), fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 14.sp)
                    }
                    if (opponentGameOver && opponentConnected) {
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)), contentAlignment = Alignment.Center) {
                            Text("GAME\nOVER", color = Color(0xFFFF5252), fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                MiniStatsRow(opponentScore, opponentLevel, opponentLines)
            }

            // My board (large)
            Column(
                modifier = Modifier.weight(2.2f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("YOU", color = ACCENT, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(3.dp))
                Box(
                    Modifier.weight(1f).aspectRatio(0.5f).border(1.dp, ACCENT.copy(0.4f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    TetrisBoardView(board = myBoard)
                    if (isGameOver) GameOverOverlay(myScore)
                    if (countdown != null) CountdownOverlay(countdown!!)
                }
                Spacer(Modifier.height(3.dp))
                StatsRow(myScore, myLevel, myLines)
            }
        }

        Spacer(Modifier.height(6.dp))

        when {
            isPlaying -> GameControls(vm)
            isGameOver -> ActionButton("다시 준비", onClick = {
                vm.backToModeSelect()
                vm.selectMode(GameMode.MULTI)
            })
            countdown != null -> { /* countdown running */ }
            !opponentConnected -> Text("상대방을 기다리는 중...", color = Color.White.copy(0.5f), fontSize = 14.sp)
            !myReady -> ActionButton("준비", onClick = { vm.setReady() })
            !opponentReady -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("준비 완료!", color = ACCENT, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("상대방 준비 대기 중...", color = Color.White.copy(0.5f), fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ─── Shared Components ───────────────────────────────

@Composable
private fun TopBar(title: String, onBack: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "back", tint = Color.White)
            }
        } else {
            Spacer(Modifier.width(36.dp))
        }
        Text(
            title, color = ACCENT, fontSize = 18.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp, textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(36.dp))
    }
}

@Composable
private fun ConnectionStatusBar(state: ConnectionState, opponentConnected: Boolean, playerCount: Int) {
    val (text, dotColor) = when {
        state == ConnectionState.CONNECTED && opponentConnected -> "서버 접속 ${playerCount}명 · 상대방 감지됨" to Color(0xFF66BB6A)
        state == ConnectionState.CONNECTED -> "서버 접속 ${playerCount}명 · 상대방 대기" to Color(0xFFFFA726)
        state == ConnectionState.CONNECTING || state == ConnectionState.RECONNECTING -> "서버 연결 중..." to Color(0xFFFFA726)
        else -> "서버 연결 안됨" to Color(0xFFEF5350)
    }
    Row(
        Modifier.fillMaxWidth().background(BG_CARD, RoundedCornerShape(6.dp)).padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        Canvas(Modifier.size(7.dp)) { drawCircle(dotColor) }
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White.copy(0.6f), fontSize = 11.sp)
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = ACCENT),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(0.6f).height(48.dp)
    ) {
        Text(text, color = BG_DARK, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun StatsRow(score: Int, level: Int, lines: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        StatLabel("SCORE", score)
        StatLabel("LEVEL", level)
        StatLabel("LINES", lines)
    }
}

@Composable
private fun MiniStatsRow(score: Int, level: Int, lines: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        StatLabel("SCR", score, small = true)
        StatLabel("LV", level, small = true)
        StatLabel("LN", lines, small = true)
    }
}

@Composable
private fun StatLabel(label: String, value: Int, small: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(0.4f), fontSize = if (small) 8.sp else 9.sp, letterSpacing = 1.sp)
        Text(value.toString(), color = Color.White, fontSize = if (small) 12.sp else 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GameOverOverlay(score: Int) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.6f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("GAME OVER", color = ACCENT, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Score: $score", color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
private fun CountdownOverlay(count: Int) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)), contentAlignment = Alignment.Center) {
        Text(count.toString(), color = ACCENT, fontSize = 64.sp, fontWeight = FontWeight.Bold)
    }
}

// ─── Board View ──────────────────────────────────────

@Composable
private fun TetrisBoardView(board: Array<IntArray>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val cellW = size.width / TetrisEngine.COLS
        val cellH = size.height / TetrisEngine.ROWS
        val cr = CornerRadius(2f, 2f)
        for (row in 0 until TetrisEngine.ROWS) {
            for (col in 0 until TetrisEngine.COLS) {
                drawRect(GRID_LINE, Offset(col * cellW, row * cellH), Size(cellW - 0.5f, cellH - 0.5f))
            }
        }
        for (row in board.indices) {
            for (col in board[row].indices) {
                val v = board[row][col]
                if (v in 1..7) {
                    val x = col * cellW; val y = row * cellH
                    drawRoundRect(PIECE_COLORS[v], Offset(x + .5f, y + .5f), Size(cellW - 1f, cellH - 1f), cr)
                    drawRoundRect(Color.White.copy(0.25f), Offset(x + .5f, y + .5f), Size(cellW - 1f, (cellH - 1f) * .35f), cr)
                }
            }
        }
    }
}

// ─── Controls with Long Press ────────────────────────

@Composable
private fun GameControls(vm: TetrisViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GameButton("◀", onAction = { vm.moveLeft() })
            GameButton("▼", onAction = { vm.moveDown() })
            GameButton("▶", onAction = { vm.moveRight() })
        }
        Spacer(Modifier.width(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GameButton("↻", onAction = { vm.rotatePiece() }, accent = true, repeatable = false)
            GameButton("⬇", onAction = { vm.hardDrop() }, accent = true, repeatable = false)
        }
    }
}

@Composable
private fun GameButton(
    text: String,
    onAction: () -> Unit,
    accent: Boolean = false,
    repeatable: Boolean = true,
    initialDelay: Long = 180L,
    repeatInterval: Long = 60L
) {
    val currentAction by rememberUpdatedState(onAction)
    val pressed = remember { mutableStateOf(false) }

    LaunchedEffect(pressed.value) {
        if (pressed.value) {
            currentAction()
            if (repeatable) {
                delay(initialDelay)
                while (pressed.value) {
                    currentAction()
                    delay(repeatInterval)
                }
            }
        }
    }

    val bg = if (accent) ACCENT.copy(0.15f) else BG_CARD
    val bgPressed = if (accent) ACCENT.copy(0.35f) else Color(0xFF2A2A42)

    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(if (pressed.value) bgPressed else bg)
            .border(1.dp, if (accent) ACCENT.copy(0.3f) else Color(0xFF333355), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed.value = true
                        tryAwaitRelease()
                        pressed.value = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (accent) ACCENT else Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
