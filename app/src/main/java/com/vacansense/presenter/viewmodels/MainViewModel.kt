package com.vacansense.presenter.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {
    val query = MutableStateFlow("Android разработчик")
    val tgToken = MutableStateFlow("6118857351:AAF5h7beTh_UcwtZZ2VLqtcYsNADl73aHFg")
    val tgChatId = MutableStateFlow("5231714303")
    val modelPath = MutableStateFlow("/storage/emulated/0/Download/qwen2.5-0.5b-instruct-q8_0.gguf")

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    fun setRunning(state: Boolean) {
        _isRunning.value = state
    }
}
