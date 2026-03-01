package com.vacansense.data.repositories

import com.vacansense.data.network.TelegramApi
import com.vacansense.domain.irepositories.ITelegramRepository

class TelegramRepository(private val api: TelegramApi) : ITelegramRepository {
    override suspend fun sendMessage(token: String, chatId: String, text: String): Boolean {
        val url = "https://api.telegram.org/bot$token/sendMessage"
        return try {
            val response = api.sendMessage(url, chatId, text)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
