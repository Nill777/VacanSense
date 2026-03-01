package com.vacansense.data.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Синглтон для пробуждения сервиса.
 * Использует Channel.CONFLATED, чтобы хранить только последний сигнал
 * (если нажали 100 раз подряд, сервис проснется 1 раз).
 */
class ProcessingTrigger {
    private val signalChannel = Channel<Unit>(Channel.CONFLATED)

    // Вызывает UI (ViewModel)
    fun wakeUp() {
        signalChannel.trySend(Unit)
    }

    // Вызывает Сервис
    // Ждет либо сигнала, либо истечения таймаута
    suspend fun waitForSignalOrTimeout(timeoutMillis: Long) {
        withTimeoutOrNull(timeoutMillis) {
            signalChannel.receive()
        }
    }
}
