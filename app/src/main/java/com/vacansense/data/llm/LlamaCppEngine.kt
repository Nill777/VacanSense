package com.vacansense.data.llm

import android.content.Context
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import com.vacansense.domain.irepositories.ILlmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LlamaCppEngine(private val context: Context) : ILlmRepository {

    private suspend fun runInference(prompt: String, system: String, modelPath: String): String =
        withContext(Dispatchers.IO) {
            var engine: InferenceEngine? = null
            try {
                engine = AiChat.getInferenceEngine(context)
                engine.loadModel(modelPath)
                engine.setSystemPrompt(system)

                val stringBuilder = StringBuilder()
                engine.sendUserPrompt(prompt).collect { token ->
                    stringBuilder.append(token)
                }
                stringBuilder.toString().trim()
            } catch (e: Exception) {
                "Error: ${e.message}"
            } finally {
                engine?.cleanUp()
                engine?.destroy()
            }
        }

    override suspend fun summarize(
        text: String,
        prompt: String,
        modelPath: String
    ): String {
        val systemPrompt =
            "Ты опытный AI-помощник HR-специалиста. Твоя задача — анализировать вакансии."
        val userPrompt = "$prompt\n\nТекст вакансии:\n${text.take(1500)}"
        return runInference(userPrompt, systemPrompt, modelPath)
    }

    override suspend fun evaluateNegative(
        text: String,
        negativeCondition: String,
        modelPath: String
    ): Int {
        val systemPrompt =
            "Ты - строгий судья-аналитик. Выдавай ответ ТОЛЬКО В ВИДЕ ЧИСЛА от 0 до 100, никаких других слов и символов."
        val prompt =
            "Требуется ли в данной вакансии это или описывает ли её: \"$negativeCondition\"?\n" +
                    "Оцени от 0 (вообще нет) до 100 (точно есть). Твой ответ должен состоять строго из одной цифры.\n" +
                    "Текст: ${text.take(1500)}"

        val result = runInference(prompt, systemPrompt, modelPath)
        // Ищем первое вхождение чисел
        val regex = Regex("\\d+")
        val match = regex.find(result)
        return match?.value?.toIntOrNull()?.coerceIn(0, 100) ?: 0
    }
}
