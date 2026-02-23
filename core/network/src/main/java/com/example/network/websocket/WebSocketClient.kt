package com.example.network.websocket

import com.example.app_config_api.AppConfig
import com.example.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp 기반 WebSocket 클라이언트.
 *
 * - 지수 백오프 자동 재연결
 * - 오프라인 메시지 큐잉 (연결 복구 시 자동 전송)
 * - OkHttp ping/pong 기반 하트비트 (pingInterval 15s)
 * - SharedFlow 기반 수신 메시지 브로드캐스팅
 */
@Singleton
class WebSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val appConfig: AppConfig
) {
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 256)
    val incomingMessages: SharedFlow<String> = _incomingMessages.asSharedFlow()

    private val messageQueue = ConcurrentLinkedQueue<String>()
    private val reconnectionStrategy = ReconnectionStrategy()

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var reconnectionJob: Job? = null
    private var isManuallyDisconnected = false

    fun connect() {
        if (_connectionState.value == ConnectionState.CONNECTED ||
            _connectionState.value == ConnectionState.CONNECTING
        ) return

        isManuallyDisconnected = false
        doConnect()
    }

    fun disconnect() {
        isManuallyDisconnected = true
        reconnectionJob?.cancel()
        webSocket?.close(NORMAL_CLOSURE, "Client disconnect")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun send(message: String): Boolean {
        return if (_connectionState.value == ConnectionState.CONNECTED) {
            webSocket?.send(message) ?: false
        } else {
            messageQueue.offer(message)
            false
        }
    }

    fun release() {
        disconnect()
        scope.cancel()
    }

    private fun doConnect() {
        _connectionState.value = ConnectionState.CONNECTING
        val request = Request.Builder()
            .url(appConfig.baseUrl)
            .build()
        webSocket = okHttpClient.newWebSocket(request, createListener())
    }

    private fun createListener() = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            _connectionState.value = ConnectionState.CONNECTED
            reconnectionStrategy.reset()
            flushMessageQueue()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            _incomingMessages.tryEmit(text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            this@WebSocketClient.webSocket = null
            _connectionState.value = ConnectionState.DISCONNECTED
            if (!isManuallyDisconnected) {
                scheduleReconnection()
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            this@WebSocketClient.webSocket = null
            _connectionState.value = ConnectionState.DISCONNECTED
            if (!isManuallyDisconnected && code != NORMAL_CLOSURE) {
                scheduleReconnection()
            }
        }
    }

    private fun scheduleReconnection() {
        if (!reconnectionStrategy.canRetry) return

        reconnectionJob?.cancel()
        reconnectionJob = scope.launch {
            _connectionState.value = ConnectionState.RECONNECTING
            val delayMs = reconnectionStrategy.nextDelay()
            delay(delayMs)
            if (isActive && !isManuallyDisconnected) {
                doConnect()
            }
        }
    }

    private fun flushMessageQueue() {
        while (messageQueue.isNotEmpty()) {
            val msg = messageQueue.poll() ?: break
            val sent = webSocket?.send(msg) ?: false
            if (!sent) {
                messageQueue.offer(msg)
                return
            }
        }
    }

    companion object {
        private const val NORMAL_CLOSURE = 1000
    }
}
