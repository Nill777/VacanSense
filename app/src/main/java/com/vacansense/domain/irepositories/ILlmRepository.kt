package com.vacansense.domain.irepositories

interface ILlmRepository {
    suspend fun summarize(text: String, modelPath: String): String
}
