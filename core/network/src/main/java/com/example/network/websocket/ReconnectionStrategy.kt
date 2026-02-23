package com.example.network.websocket

import kotlin.math.min
import kotlin.random.Random

/**
 * 지수 백오프 + 지터를 활용한 재연결 전략.
 * 대규모 트래픽 환경에서 Thundering Herd 문제를 방지한다.
 */
class ReconnectionStrategy(
    private val initialDelayMs: Long = 1_000L,
    private val maxDelayMs: Long = 30_000L,
    private val multiplier: Double = 2.0,
    private val maxRetries: Int = Int.MAX_VALUE
) {
    private var currentDelay = initialDelayMs
    private var retryCount = 0

    val canRetry: Boolean get() = retryCount < maxRetries

    fun nextDelay(): Long {
        if (!canRetry) return maxDelayMs
        val delay = currentDelay
        currentDelay = min((currentDelay * multiplier).toLong(), maxDelayMs)
        retryCount++
        val jitter = if (delay > 0) Random.nextLong(0, delay / 4 + 1) else 0
        return delay + jitter
    }

    fun reset() {
        currentDelay = initialDelayMs
        retryCount = 0
    }
}
