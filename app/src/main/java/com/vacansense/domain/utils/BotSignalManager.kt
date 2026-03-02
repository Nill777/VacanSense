package com.vacansense.domain.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class BotSignalManager {
    private val _wakeUpSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val wakeUpSignal = _wakeUpSignal.asSharedFlow()

    fun wakeUp() {
        _wakeUpSignal.tryEmit(Unit)
    }
}
