package com.example.kakaotalk.ui.gacha

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kakaotalk.data.repository.ChatRepository
import com.example.model.ConnectionState
import com.example.model.SocketEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GachaCell(
    val index: Int,
    val rank: Int? = null,
    val pickedBy: String? = null,
    val heldBy: String? = null
) {
    val isPicked: Boolean get() = rank != null
    val isHeld: Boolean get() = !heldBy.isNullOrBlank()
}

data class CelebrationEvent(
    val rank: Int,
    val byUser: String?,
    val index: Int,
    val nonce: Long = System.currentTimeMillis()
)

@HiltViewModel
class GachaViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _cells = MutableStateFlow(List(CELL_COUNT) { GachaCell(index = it) })
    val cells: StateFlow<List<GachaCell>> = _cells.asStateFlow()

    private val _lastResult = MutableStateFlow<Int?>(null)
    val lastResult: StateFlow<Int?> = _lastResult.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _celebration = MutableStateFlow<CelebrationEvent?>(null)
    val celebration: StateFlow<CelebrationEvent?> = _celebration.asStateFlow()

    private val _remainingDrawCount = MutableStateFlow(0)
    val remainingDrawCount: StateFlow<Int> = _remainingDrawCount.asStateFlow()

    private val _totalDrawCount = MutableStateFlow(0)
    val totalDrawCount: StateFlow<Int> = _totalDrawCount.asStateFlow()

    private val _showSetCountDialog = MutableStateFlow(true)
    val showSetCountDialog: StateFlow<Boolean> = _showSetCountDialog.asStateFlow()

    private val _confirmPickIndex = MutableStateFlow<Int?>(null)
    val confirmPickIndex: StateFlow<Int?> = _confirmPickIndex.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = chatRepository.connectionState

    private var pendingHoldIndex: Int? = null

    init {
        chatRepository.connect()

        viewModelScope.launch {
            chatRepository.connectionState.collect { state ->
                if (state == ConnectionState.CONNECTED) {
                    chatRepository.requestDrawSync()
                }
            }
        }

        viewModelScope.launch {
            chatRepository.gachaEvents.collect { event ->
                when (event) {
                    is SocketEvent.DrawSyncReceived -> applySync(event.boardData)
                    is SocketEvent.DrawPicked -> applyPicked(event)
                    is SocketEvent.DrawHeld -> applyHeld(event)
                    is SocketEvent.DrawReleased -> applyReleased(event)
                    is SocketEvent.DrawLimitUpdated -> {
                        _totalDrawCount.value = event.total
                        _remainingDrawCount.value = event.remaining
                        _showSetCountDialog.value = event.total <= 0
                    }
                    is SocketEvent.DrawReseted -> applyReset()
                    is SocketEvent.DrawRejected -> {
                        _message.value = event.reason
                        pendingHoldIndex = null
                        _confirmPickIndex.value = null
                    }
                    else -> Unit
                }
            }
        }
    }

    val allPicked: Boolean
        get() = _cells.value.all { it.isPicked }

    fun requestSetDrawCount(count: Int) {
        if (count <= 0) {
            _message.value = "뽑기 횟수는 1회 이상이어야 합니다."
            return
        }
        // 서버 응답 전에도 즉시 시작 상태가 보이도록 낙관적 업데이트
        _totalDrawCount.value = count
        _remainingDrawCount.value = count
        _showSetCountDialog.value = false
        _message.value = "${count}회 뽑기 시작!"
        chatRepository.setDrawLimit(count)
        chatRepository.requestDrawSync()
    }

    fun requestPick(index: Int) {
        if (_remainingDrawCount.value <= 0) {
            _message.value = "남은 뽑기 횟수가 없습니다."
            return
        }
        val cell = _cells.value.getOrNull(index) ?: return
        if (cell.isPicked) {
            _message.value = "이미 뽑힌 칸입니다."
            return
        }
        if (cell.isHeld) {
            _message.value = "다른 사용자가 선택 중인 칸입니다."
            return
        }
        pendingHoldIndex = index
        chatRepository.holdDraw(index)
    }

    fun confirmPick() {
        val idx = _confirmPickIndex.value ?: return
        chatRepository.pickDraw(idx)
        _confirmPickIndex.value = null
        pendingHoldIndex = null
    }

    fun cancelPick() {
        val idx = _confirmPickIndex.value ?: pendingHoldIndex ?: return
        chatRepository.releaseDraw(idx)
        _confirmPickIndex.value = null
        pendingHoldIndex = null
    }

    fun requestReset() {
        // 클릭 즉시 새 라운드 화면으로 전환 (서버 응답 대기 없이 UX 반영)
        applyReset()
        _message.value = "새 뽑기판으로 리셋 중..."
        chatRepository.resetDraw()
        viewModelScope.launch {
            delay(500)
            chatRepository.requestDrawSync()
        }
    }

    fun dismissMessage() {
        _message.value = null
    }

    fun consumeCelebration() {
        _celebration.value = null
    }

    private fun applySync(boardData: String) {
        val split = boardData.split(";")
        val pickedData = split.getOrNull(0).orEmpty()
        val holdData = split.getOrNull(1).orEmpty()

        val rankMap = mutableMapOf<Int, Int>()
        pickedData.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { token ->
                val parts = token.split(":")
                if (parts.size == 2) {
                    val idx = parts[0].toIntOrNull()
                    val rank = parts[1].toIntOrNull()
                    if (idx != null && rank != null && idx in 0 until CELL_COUNT) {
                        rankMap[idx] = rank
                    }
                }
            }

        val holdMap = mutableMapOf<Int, String>()
        holdData.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { token ->
                val parts = token.split(":")
                if (parts.size == 2) {
                    val idx = parts[0].toIntOrNull()
                    val by = parts[1]
                    if (idx != null && idx in 0 until CELL_COUNT) holdMap[idx] = by
                }
            }

        _cells.value = List(CELL_COUNT) { index ->
            GachaCell(
                index = index,
                rank = rankMap[index],
                pickedBy = null,
                heldBy = holdMap[index]
            )
        }
    }

    private fun applyPicked(event: SocketEvent.DrawPicked) {
        if (event.index !in 0 until CELL_COUNT) return
        _cells.value = _cells.value.map { cell ->
            if (cell.index == event.index) {
                cell.copy(rank = event.rank, pickedBy = event.byUser, heldBy = null)
            } else {
                cell
            }
        }
        _lastResult.value = event.rank
        _celebration.value = CelebrationEvent(rank = event.rank, byUser = event.byUser, index = event.index)
        _message.value = "${event.byUser ?: "사용자"} 님이 ${event.index + 1}번 칸에서 ${event.rank}등을 뽑았어요!"
        if (_confirmPickIndex.value == event.index) _confirmPickIndex.value = null
        if (pendingHoldIndex == event.index) pendingHoldIndex = null
    }

    private fun applyHeld(event: SocketEvent.DrawHeld) {
        if (event.index !in 0 until CELL_COUNT) return
        _cells.value = _cells.value.map { cell ->
            if (cell.index == event.index && !cell.isPicked) cell.copy(heldBy = event.byUser) else cell
        }
        if (pendingHoldIndex == event.index) {
            _confirmPickIndex.value = event.index
        }
    }

    private fun applyReleased(event: SocketEvent.DrawReleased) {
        if (event.index !in 0 until CELL_COUNT) return
        _cells.value = _cells.value.map { cell ->
            if (cell.index == event.index && !cell.isPicked) cell.copy(heldBy = null) else cell
        }
    }

    private fun applyReset() {
        _cells.value = List(CELL_COUNT) { GachaCell(index = it) }
        _lastResult.value = null
        _totalDrawCount.value = 0
        _remainingDrawCount.value = 0
        _showSetCountDialog.value = true
        _message.value = "새 뽑기판으로 리셋되었습니다."
        _confirmPickIndex.value = null
        pendingHoldIndex = null
    }

    companion object {
        const val GRID_SIZE = 4
        const val CELL_COUNT = GRID_SIZE * GRID_SIZE
    }
}
