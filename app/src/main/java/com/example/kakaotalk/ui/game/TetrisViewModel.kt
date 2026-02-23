package com.example.kakaotalk.ui.game

import android.util.Log
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

enum class GameMode { SINGLE, MULTI }

@HiltViewModel
class TetrisViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val engine = TetrisEngine()

    private val _gameMode = MutableStateFlow<GameMode?>(null)
    val gameMode: StateFlow<GameMode?> = _gameMode.asStateFlow()

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

    private val _opponentBoard = MutableStateFlow(emptyBoard())
    val opponentBoard: StateFlow<Array<IntArray>> = _opponentBoard.asStateFlow()

    private val _opponentScore = MutableStateFlow(0)
    val opponentScore: StateFlow<Int> = _opponentScore.asStateFlow()

    private val _opponentLevel = MutableStateFlow(1)
    val opponentLevel: StateFlow<Int> = _opponentLevel.asStateFlow()

    private val _opponentLines = MutableStateFlow(0)
    val opponentLines: StateFlow<Int> = _opponentLines.asStateFlow()

    private val _opponentConnected = MutableStateFlow(false)
    val opponentConnected: StateFlow<Boolean> = _opponentConnected.asStateFlow()

    private val _opponentReady = MutableStateFlow(false)
    val opponentReady: StateFlow<Boolean> = _opponentReady.asStateFlow()

    private val _opponentGameOver = MutableStateFlow(false)
    val opponentGameOver: StateFlow<Boolean> = _opponentGameOver.asStateFlow()

    private val _myReady = MutableStateFlow(false)
    val myReady: StateFlow<Boolean> = _myReady.asStateFlow()

    private val _countdown = MutableStateFlow<Int?>(null)
    val countdown: StateFlow<Int?> = _countdown.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = chatRepository.connectionState

    private var gameJob: Job? = null
    private var presenceJob: Job? = null

    init {
        chatRepository.connect()

        viewModelScope.launch {
            chatRepository.connectionState.collect { state ->
                Log.d(TAG, "Connection: $state")
                if (state == ConnectionState.CONNECTED && _gameMode.value == GameMode.MULTI) {
                    sendCurrentState()
                }
            }
        }

        viewModelScope.launch {
            chatRepository.gameEvents.collect { event ->
                when (event) {
                    is SocketEvent.GameStateReceived -> {
                        try {
                            val state = TetrisEngine.deserializeBoard(event.boardData)
                            _opponentBoard.value = state.board
                            _opponentScore.value = state.score
                            _opponentLines.value = state.linesCleared
                            _opponentLevel.value = state.level
                            _opponentConnected.value = true
                            _opponentGameOver.value = state.isGameOver
                        } catch (e: Exception) {
                            Log.e(TAG, "Deserialize failed: ${e.message}")
                        }
                    }
                    is SocketEvent.OpponentReady -> {
                        _opponentReady.value = true
                        if (_myReady.value) startCountdownAndPlay()
                    }
                    is SocketEvent.OpponentGameOver -> {
                        _opponentGameOver.value = true
                    }
                    else -> Unit
                }
            }
        }
    }

    fun selectMode(mode: GameMode) {
        _gameMode.value = mode
        if (mode == GameMode.MULTI) {
            startPresenceHeartbeat()
        }
    }

    fun backToModeSelect() {
        gameJob?.cancel()
        presenceJob?.cancel()
        engine.reset()
        _gameMode.value = null
        _isPlaying.value = false
        _isGameOver.value = false
        _myReady.value = false
        _opponentReady.value = false
        _opponentConnected.value = false
        _opponentGameOver.value = false
        _opponentBoard.value = emptyBoard()
        _opponentScore.value = 0
        _opponentLevel.value = 1
        _opponentLines.value = 0
        _countdown.value = null
        _myScore.value = 0
        _myLevel.value = 1
        _myLines.value = 0
        updateState()
    }

    fun setReady() {
        _myReady.value = true
        chatRepository.sendGameReady()
        if (_opponentReady.value) {
            startCountdownAndPlay()
        }
    }

    fun startSingleGame() {
        engine.reset()
        _isPlaying.value = true
        _isGameOver.value = false
        updateState()
        startGameLoop()
    }

    fun moveLeft() { if (engine.moveLeft()) onBoardChanged() }
    fun moveRight() { if (engine.moveRight()) onBoardChanged() }
    fun moveDown() { if (engine.moveDown()) onBoardChanged() }
    fun rotatePiece() { if (engine.rotate()) onBoardChanged() }
    fun hardDrop() { if (engine.hardDrop()) onBoardChanged() }

    private fun onBoardChanged() {
        updateState()
        if (_gameMode.value == GameMode.MULTI) sendCurrentState()
    }

    private fun startCountdownAndPlay() {
        viewModelScope.launch {
            _countdown.value = 3; delay(800)
            _countdown.value = 2; delay(800)
            _countdown.value = 1; delay(800)
            _countdown.value = null
            engine.reset()
            _isPlaying.value = true
            _isGameOver.value = false
            updateState()
            startGameLoop()
        }
    }

    private fun startGameLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (!engine.isGameOver) {
                delay(getTickInterval())
                engine.tick()
                updateState()
                if (_gameMode.value == GameMode.MULTI) sendCurrentState()
            }
            _isGameOver.value = true
            _isPlaying.value = false
            if (_gameMode.value == GameMode.MULTI) {
                sendCurrentState()
                chatRepository.sendGameOver(engine.score)
            }
        }
    }

    private fun startPresenceHeartbeat() {
        presenceJob?.cancel()
        presenceJob = viewModelScope.launch {
            while (true) {
                delay(PRESENCE_INTERVAL_MS)
                if (chatRepository.connectionState.value == ConnectionState.CONNECTED) {
                    sendCurrentState()
                }
            }
        }
    }

    private fun updateState() {
        _myBoard.value = engine.getBoardWithPiece()
        _myScore.value = engine.score
        _myLevel.value = engine.level
        _myLines.value = engine.linesCleared
    }

    private fun sendCurrentState() {
        chatRepository.sendGameState(engine.serializeBoard())
    }

    private fun getTickInterval(): Long = maxOf(100L, 1000L - (engine.level - 1) * 80L)

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
        presenceJob?.cancel()
    }

    companion object {
        private const val TAG = "TetrisVM"
        private const val PRESENCE_INTERVAL_MS = 2000L
        private fun emptyBoard() = Array(TetrisEngine.ROWS) { IntArray(TetrisEngine.COLS) }
    }
}
