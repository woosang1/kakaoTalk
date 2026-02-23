package com.example.kakaotalk.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kakaotalk.data.repository.ChatRepository
import com.example.model.ConnectionState
import com.example.model.SocketEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TetrisViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val engine = TetrisEngine()

    private val _myBoard = MutableStateFlow(engine.getBoardWithPiece())
    val myBoard: StateFlow<Array<IntArray>> = _myBoard.asStateFlow()

    private val _myScore = MutableStateFlow(0)
    val myScore: StateFlow<Int> = _myScore.asStateFlow()

    private val _myLevel = MutableStateFlow(1)
    val myLevel: StateFlow<Int> = _myLevel.asStateFlow()

    private val _myLines = MutableStateFlow(0)
    val myLines: StateFlow<Int> = _myLines.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    private val _opponentBoard = MutableStateFlow(Array(TetrisEngine.ROWS) { IntArray(TetrisEngine.COLS) })
    val opponentBoard: StateFlow<Array<IntArray>> = _opponentBoard.asStateFlow()

    private val _opponentScore = MutableStateFlow(0)
    val opponentScore: StateFlow<Int> = _opponentScore.asStateFlow()

    private val _opponentLevel = MutableStateFlow(1)
    val opponentLevel: StateFlow<Int> = _opponentLevel.asStateFlow()

    private val _opponentLines = MutableStateFlow(0)
    val opponentLines: StateFlow<Int> = _opponentLines.asStateFlow()

    private val _opponentConnected = MutableStateFlow(false)
    val opponentConnected: StateFlow<Boolean> = _opponentConnected.asStateFlow()

    private val _opponentGameOver = MutableStateFlow(false)
    val opponentGameOver: StateFlow<Boolean> = _opponentGameOver.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = chatRepository.connectionState

    private var gameJob: Job? = null

    init {
        chatRepository.connect()

        viewModelScope.launch {
            chatRepository.connectionState.collect { state ->
                if (state == ConnectionState.CONNECTED && _isPlaying.value) {
                    chatRepository.sendGameState(engine.serializeBoard())
                }
            }
        }

        viewModelScope.launch {
            chatRepository.gameEvents.collect { event ->
                when (event) {
                    is SocketEvent.GameStateReceived -> {
                        val state = TetrisEngine.deserializeBoard(event.boardData)
                        _opponentBoard.value = state.board
                        _opponentScore.value = state.score
                        _opponentLines.value = state.linesCleared
                        _opponentLevel.value = state.level
                        _opponentConnected.value = true
                        _opponentGameOver.value = state.isGameOver
                    }
                    is SocketEvent.OpponentGameOver -> {
                        _opponentGameOver.value = true
                    }
                    else -> Unit
                }
            }
        }
    }

    fun startGame() {
        engine.reset()
        _isPlaying.value = true
        _isGameOver.value = false
        _opponentGameOver.value = false
        updateAndSend()

        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (!engine.isGameOver) {
                delay(getTickInterval())
                engine.tick()
                updateAndSend()
            }
            _isGameOver.value = true
            _isPlaying.value = false
            chatRepository.sendGameState(engine.serializeBoard())
            chatRepository.sendGameOver(engine.score)
        }
    }

    fun moveLeft() {
        if (engine.moveLeft()) updateAndSend()
    }

    fun moveRight() {
        if (engine.moveRight()) updateAndSend()
    }

    fun moveDown() {
        if (engine.moveDown()) updateAndSend()
    }

    fun rotatePiece() {
        if (engine.rotate()) updateAndSend()
    }

    fun hardDrop() {
        if (engine.hardDrop()) updateAndSend()
    }

    private fun updateState() {
        _myBoard.value = engine.getBoardWithPiece()
        _myScore.value = engine.score
        _myLevel.value = engine.level
        _myLines.value = engine.linesCleared
    }

    private fun updateAndSend() {
        updateState()
        chatRepository.sendGameState(engine.serializeBoard())
    }

    private fun getTickInterval(): Long {
        return maxOf(100L, 1000L - (engine.level - 1) * 80L)
    }

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
    }
}
