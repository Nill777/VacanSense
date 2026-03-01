package com.vacansense

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BotForegroundService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val query = intent?.getStringExtra("QUERY") ?: "Android разработчик"
        val tgToken = intent?.getStringExtra("TOKEN") ?: ""
        val tgChatId = intent?.getStringExtra("CHAT_ID") ?: ""
        val modelPath = intent?.getStringExtra("MODEL_PATH") ?: ""

        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "BOT_CHANNEL")
            .setContentTitle("VacanSense Bot")
            .setContentText("Ищем и обрабатываем вакансии...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1, notification)

        // Блокируем сон процессора
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock =
            powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VacanSense::BotWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L)

        // Достаем UseCase через Manual DI (AppContainer)
        val appContainer = (application as App).container
        val processUseCase = appContainer.processVacanciesUseCase

        serviceScope.launch {
            while (isActive) {
                processUseCase.execute(query, tgToken, tgChatId, modelPath)
                delay(1 * 60 * 1000) // Ищем каждую минуту для теста, потом лучше сделать 15 мин
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        wakeLock?.takeIf { it.isHeld }?.release()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "BOT_CHANNEL",
                "Bot Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
