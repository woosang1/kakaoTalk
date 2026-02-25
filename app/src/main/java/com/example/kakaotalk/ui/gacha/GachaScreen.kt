package com.example.kakaotalk.ui.gacha

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.model.ConnectionState
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GachaScreen(
    modifier: Modifier = Modifier,
    viewModel: GachaViewModel = hiltViewModel()
) {
    val cells by viewModel.cells.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()
    val message by viewModel.message.collectAsState()
    val celebration by viewModel.celebration.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val totalCount by viewModel.totalDrawCount.collectAsState()
    val remainCount by viewModel.remainingDrawCount.collectAsState()
    val showSetCountDialog by viewModel.showSetCountDialog.collectAsState()
    val confirmPickIndex by viewModel.confirmPickIndex.collectAsState()

    val openedCount = remember(cells) { cells.count { it.isPicked } }
    val remainingCellCount = remember(cells) { cells.size - openedCount }
    val allPicked = remember(cells) { cells.all { it.isPicked } }

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(1800)
            viewModel.dismissMessage()
        }
    }
    LaunchedEffect(celebration?.nonce) {
        if (celebration != null) {
            kotlinx.coroutines.delay(2400)
            viewModel.consumeCelebration()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B1220), Color(0xFF111827), Color(0xFF1E293B))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Header(
                connectionState = connectionState,
                openedCount = openedCount,
                remainingCellCount = remainingCellCount,
                totalDrawCount = totalCount,
                remainDrawCount = remainCount
            )
            Spacer(Modifier.height(12.dp))

            if (allPicked) {
                Button(
                    onClick = viewModel::requestReset,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("새 뽑기창 리셋", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(GachaViewModel.GRID_SIZE),
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cells, key = { it.index }) { cell ->
                    DrawCell(
                        cell = cell,
                        enabled = remainCount > 0 && totalCount > 0 && !cell.isPicked && !cell.isHeld,
                        onClick = { viewModel.requestPick(cell.index) }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            ResultCard(lastResult = lastResult, message = message)
            Spacer(Modifier.height(10.dp))
            RankGuide()
            Spacer(Modifier.height(18.dp))
        }

        celebration?.let { CelebrationDialog(event = it, onDismiss = viewModel::consumeCelebration) }
        if (showSetCountDialog) {
            SetCountDialog(
                onConfirm = viewModel::requestSetDrawCount
            )
        }
        if (confirmPickIndex != null) {
            ConfirmPickDialog(
                index = confirmPickIndex!!,
                onYes = viewModel::confirmPick,
                onNo = viewModel::cancelPick
            )
        }
    }
}

@Composable
private fun Header(
    connectionState: ConnectionState,
    openedCount: Int,
    remainingCellCount: Int,
    totalDrawCount: Int,
    remainDrawCount: Int
) {
    Text("LUCKY DRAW LIVE", color = Color(0xFFFDE68A), fontWeight = FontWeight.ExtraBold, fontSize = 27.sp)
    Spacer(Modifier.height(6.dp))
    Text(
        text = connectionText(connectionState),
        color = if (connectionState == ConnectionState.CONNECTED) Color(0xFF34D399) else Color(0xFFF59E0B),
        fontSize = 13.sp
    )
    Spacer(Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.07f)).padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HeaderMetric("오픈", openedCount.toString(), Color(0xFF60A5FA))
        HeaderMetric("남은 칸", remainingCellCount.toString(), Color(0xFFF59E0B))
        HeaderMetric("내 횟수", "$remainDrawCount/$totalDrawCount", Color(0xFF34D399))
    }
}

@Composable
private fun HeaderMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = Color.White.copy(alpha = 0.72f), fontSize = 11.sp)
    }
}

@Composable
private fun DrawCell(
    cell: GachaCell,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val rankColor = rankColor(cell.rank)
    val bg = when {
        cell.isPicked -> Brush.linearGradient(listOf(Color(0xFF111827), Color(0xFF1F2937)))
        cell.isHeld -> Brush.linearGradient(listOf(Color(0xFF4B5563), Color(0xFF6B7280)))
        else -> Brush.linearGradient(listOf(Color(0xFF374151), Color(0xFF4B5563)))
    }
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(14.dp))
            .background(bg).border(1.dp, rankColor.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            cell.isPicked -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("OPEN", color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp)
                    Text("${cell.rank}등", color = rankColor, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                }
            }
            cell.isHeld -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LOCK", color = Color(0xFFFDE68A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("선택중", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(cell.heldBy ?: "", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                }
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎁", fontSize = 24.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("${cell.index + 1}번", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ResultCard(lastResult: Int?, message: String?) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("최근 결과", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (lastResult == null) "아직 뽑기 결과가 없습니다" else "🎉 ${lastResult}등 당첨!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
            if (!message.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(message, color = Color(0xFFCBD5E1), fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun RankGuide() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("등수 가이드", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RankChip("1등", rankColor(1))
            RankChip("2등", rankColor(2))
            RankChip("3등", rankColor(3))
            RankChip("4등", rankColor(4))
        }
    }
}

@Composable
private fun RankChip(text: String, color: Color) {
    Box(
        modifier = Modifier.background(color.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
            .border(1.dp, color.copy(alpha = 0.65f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) { Text(text = text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun SetCountDialog(onConfirm: (Int) -> Unit) {
    var selected by remember { mutableIntStateOf(3) }
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("몇 번 뽑으시겠어요?", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(1, 3, 5, 10).forEach { count ->
                        OutlinedButton(
                            onClick = { selected = count },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected == count) Color(0xFF1D4ED8) else Color.Transparent
                            )
                        ) {
                            Text("${count}회", color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(onClick = { onConfirm(selected) }) {
                    Text("시작하기")
                }
            }
        }
    }
}

@Composable
private fun ConfirmPickDialog(index: Int, onYes: () -> Unit, onNo: () -> Unit) {
    Dialog(onDismissRequest = onNo) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${index + 1}번 뽑기를 선택하시겠습니까?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onNo, modifier = Modifier.width(120.dp)) { Text("아니오") }
                    Button(onClick = onYes, modifier = Modifier.width(120.dp)) { Text("예") }
                }
            }
        }
    }
}

@Composable
private fun CelebrationDialog(event: CelebrationEvent, onDismiss: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "celebration")
    val pulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
            ConfettiLayer()
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth().scale(pulse).border(2.dp, rankColor(event.rank), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("축하합니다!", color = Color(0xFFFDE68A), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(10.dp))
                    Text("${event.rank}등 당첨", color = rankColor(event.rank), fontSize = 34.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${event.byUser ?: "사용자"} 님의 ${event.index + 1}번 칸 결과입니다",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("🎆🎉", fontSize = 28.sp, modifier = Modifier.alpha(0.95f))
                }
            }
        }
    }
}

@Composable
private fun ConfettiLayer() {
    val particles = remember {
        List(64) {
            ConfettiParticle(
                x = Random.nextFloat(),
                delay = Random.nextFloat(),
                speed = 0.45f + Random.nextFloat() * 0.8f,
                size = 3f + Random.nextFloat() * 5f,
                drift = 6f + Random.nextFloat() * 18f,
                color = listOf(
                    Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFF10B981),
                    Color(0xFF3B82F6), Color(0xFFE879F9), Color(0xFFFDE68A)
                )[it % 6]
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "confetti")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing), RepeatMode.Restart),
        label = "confettiT"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val y = (((t * p.speed) + p.delay) % 1f) * size.height
            val wobble = sin((t * 8f + p.delay * 10f).toDouble()).toFloat() * p.drift
            val x = (p.x * size.width + wobble).coerceIn(0f, size.width)
            drawCircle(color = p.color, radius = p.size, center = Offset(x, y))
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val delay: Float,
    val speed: Float,
    val size: Float,
    val drift: Float,
    val color: Color
)

private fun rankColor(rank: Int?): Color = when (rank) {
    1 -> Color(0xFFEF4444)
    2 -> Color(0xFFF59E0B)
    3 -> Color(0xFF10B981)
    4 -> Color(0xFF3B82F6)
    else -> Color(0xFF6B7280)
}

private fun connectionText(state: ConnectionState): String = when (state) {
    ConnectionState.CONNECTED -> "서버 연결됨"
    ConnectionState.CONNECTING,
    ConnectionState.RECONNECTING -> "서버 연결 중..."
    ConnectionState.DISCONNECTED -> "서버 연결 안됨"
}
