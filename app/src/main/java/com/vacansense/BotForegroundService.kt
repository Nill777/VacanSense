package com.vacansense

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
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
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "BOT_CHANNEL")
            .setContentTitle("VacanSense Bot")
            .setContentText("Бот активен. Обработка вакансий...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock =
            powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VacanSense::BotWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L) // 10 минут таймаут (на всякий случай)

        val appContainer = (application as App).container
        val processUseCase = appContainer.processVacanciesUseCase
        val settingsRepo = appContainer.settingsRepository
        val downloadManager = appContainer.modelDownloadManager

        serviceScope.launch {
            while (isActive) {
                try {
                    val settings = settingsRepo.getSettings()
                    val selectedFile = settings.selectedModelFileName

                    if (selectedFile.isNotBlank()) {
                        val absPath = downloadManager.getAbsolutePath(selectedFile)

                        // 1. Сначала ищем новые вакансии и сохраняем их в базу
                        Log.i("BotService", "=== Начало цикла: Поиск новых вакансий ===")
                        processUseCase.fetchAndSave()

                        // 2. Теперь обрабатываем очередь, пока она не опустеет
                        var hasMoreWork = true
                        while (isActive && hasMoreWork) {
                            Log.i("BotService", "Запуск обработки одной вакансии...")
                            // processNext вернет true, если вакансия была обработана
                            // вернет false, если список пуст
                            hasMoreWork = processUseCase.processNext(absPath)

                            if (hasMoreWork) {
                                // Если вакансия обработана, делаем небольшую паузу перед следующей,
                                // чтобы дать процессору остыть и не спамить в телеграм слишком часто.
                                Log.i(
                                    "BotService",
                                    "Вакансия обработана. Пауза 10 сек перед следующей..."
                                )
                            }
                        }
                        Log.i("BotService", "Очередь пуста.")
                    } else {
                        Log.w("BotService", "Модель не выбрана, ожидание...")
                    }
                } catch (e: Exception) {
                    Log.e("BotService", "Критическая ошибка в цикле сервиса", e)
                }

                // 3. Когда все вакансии обработаны, засыпаем надолго (например, 5 минут)
                Log.i("BotService", "=== Цикл завершен. Сон 5 минут ===")
                delay(5 * 60 * 1000)
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
            manager?.createNotificationChannel(channel)
        }
    }
}
