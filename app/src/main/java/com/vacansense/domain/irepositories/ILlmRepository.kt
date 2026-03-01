package com.vacansense.domain.irepositories

interface ILlmRepository {
    suspend fun summarize(text: String, prompt: String, modelPath: String): String
    // Оценивает текст и возвращает число от 0 до 100
    suspend fun evaluateNegative(text: String, negativeCondition: String, modelPath: String): Int
}
