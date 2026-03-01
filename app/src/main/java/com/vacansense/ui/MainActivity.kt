package com.vacansense.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.vacansense.BotForegroundService
import com.vacansense.presenter.viewmodels.MainViewModel
import com.vacansense.ui.screens.BotScreen
import com.vacansense.ui.theme.VacanSenseTheme

class MainActivity : ComponentActivity() {

    // Инициализация ViewModel стандартными средствами AndroidX (без Koin)
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        setContent {
            VacanSenseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BotScreen(viewModel) { start ->
                        if (start) startBot() else stopBot()
                    }
                }
            }
        }
    }

    private fun startBot() {
        val intent = Intent(this, BotForegroundService::class.java).apply {
            putExtra("QUERY", viewModel.query.value)
            putExtra("TOKEN", viewModel.tgToken.value)
            putExtra("CHAT_ID", viewModel.tgChatId.value)
            putExtra("MODEL_PATH", viewModel.modelPath.value)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        viewModel.setRunning(true)
    }

    private fun stopBot() {
        stopService(Intent(this, BotForegroundService::class.java))
        viewModel.setRunning(false)
    }

    private fun requestPermissions() {
        val launcher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        }
    }
}
