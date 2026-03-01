package com.vacansense.domain.irepositories

interface ITelegramRepository {
    suspend fun sendMessage(token: String, chatId: String, text: String): Boolean
}
