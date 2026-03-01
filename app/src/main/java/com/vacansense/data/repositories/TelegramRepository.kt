package com.vacansense.data.repositories

import android.util.Log
import com.vacansense.data.network.TelegramApi
import com.vacansense.domain.irepositories.ITelegramRepository

class TelegramRepository(private val api: TelegramApi) : ITelegramRepository {
    override suspend fun sendMessage(token: String, chatId: String, text: String): Boolean {
        Log.d("TelegramRepository", "Отправка сообщения в чат $chatId. Токен: ${token.take(5)}...")

        val url = "https://api.telegram.org/bot$token/sendMessage"
        return try {
            val response = api.sendMessage(url, chatId, text)
            if (response.isSuccessful) {
                Log.d("TelegramRepository", "Сообщение успешно отправлено!")
                true
            } else {
                Log.e(
                    "TelegramRepository",
                    "Ошибка отправки: Код ${response.code()}, Тело: ${
                        response.errorBody()?.string()
                    }"
                )
                false
            }
        } catch (e: Exception) {
            Log.e("TelegramRepository", "Исключение при отправке в Telegram", e)
            false
        }
    }
}