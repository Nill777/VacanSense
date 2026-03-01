package com.vacansense.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vacansense.App
import com.vacansense.BotForegroundService
import com.vacansense.presenter.viewmodels.MainViewModel
import com.vacansense.ui.screens.FiltersScreen
import com.vacansense.ui.screens.MainScreen
import com.vacansense.ui.screens.VacancyListScreen
import com.vacansense.ui.theme.VacanSenseTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        val appContainer = (application as App).container
        MainViewModel.Factory(
            appContainer.settingsRepository,
            appContainer.modelDownloadManager,
            appContainer.vacancyRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Мы больше не запрашиваем файловые хранилища!
        // Разрешение POST_NOTIFICATIONS для ForegroundService:
        val launcher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }

        setContent {
            VacanSenseTheme {
                val navController = rememberNavController()
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") { MainScreen(viewModel, navController, ::toggleBot) }
                        composable("filters") { FiltersScreen(viewModel, navController) }
                        composable("vacancies") { VacancyListScreen(viewModel, navController) }
                    }
                }
            }
        }
    }

    private fun toggleBot(shouldRun: Boolean) {
        if (shouldRun) {
            val intent = Intent(this, BotForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(
                intent
            )
            viewModel.setRunning(true)
        } else {
            stopService(Intent(this, BotForegroundService::class.java))
            viewModel.setRunning(false)
        }
    }
}
