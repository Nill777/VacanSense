package com.vacansense.data.llm

import android.content.Context
import android.os.Process
import android.util.Log
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import com.vacansense.domain.irepositories.ILlmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class LlamaCppEngine(private val context: Context) : ILlmRepository {

    private suspend fun runInference(prompt: String, system: String, modelPath: String): String =
        withContext(Dispatchers.Default) {
            // Запоминаем оригинальный приоритет потока из пула корутин
            val originalPriority = Process.getThreadPriority(Process.myTid())

            var engine: InferenceEngine? = null
            try {
                // Понижаем приоритет текущего потока до фонового уровня.
                // С++ потоки нейросети унаследуют этот низкий приоритет и не будут
                // отбирать ресурсы у интерфейса приложения.
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)

                Log.d("LlamaCppEngine", "Инициализация движка...")
                engine = AiChat.getInferenceEngine(context)

                Log.d("LlamaCppEngine", "Загрузка модели: $modelPath")
                engine.loadModel(modelPath)
                engine.setSystemPrompt(system)

                val stringBuilder = StringBuilder()
                Log.d("LlamaCppEngine", "Генерация ответа...")

                engine.sendUserPrompt(prompt).collect { token ->
                    stringBuilder.append(token)
                    yield()
                }

                stringBuilder.toString().trim()
            } catch (e: Exception) {
                Log.e("LlamaCppEngine", "Ошибка инференса", e)
                "Error: ${e.message}"
            } finally {
                // Обязательно возвращаем потоку нормальный приоритет, чтобы не сломать пул Dispatchers.Default
                Process.setThreadPriority(originalPriority)

                try {
                    engine?.cleanUp()
                    engine?.destroy()
                    Log.d("LlamaCppEngine", "Модель выгружена из памяти")
                } catch (e: Exception) {
                    Log.e("LlamaCppEngine", "Ошибка при очистке", e)
                }
            }
        }

    override suspend fun summarize(
        text: String,
        prompt: String,
        modelPath: String
    ): String {
        val systemPrompt =
            "Ты опытный помощник HR-специалиста. Твоя задача — анализировать вакансии и делать выжимки из вакансий. Выполняй инструкции точно и строго в заданном формате, без лишних приветствий и воды."
        val userPrompt = "$prompt\n\nТекст вакансии:\n${text.take(1500)}"
        return runInference(userPrompt, systemPrompt, modelPath)
    }

    override suspend fun evaluateNegative(
        text: String,
        negativeCondition: String,
        modelPath: String
    ): Int {
        val systemPrompt =
            "Ты - строгий судья-аналитик. Твоя единственная задача - выдать вероятность в виде ОДНОГО числа от 0 до 100. Запрещено писать любой текст, слова, пояснения или спецсимволы."
        val prompt =
            "Требуется ли в данной вакансии это, содержит ли эти задачи, описывает ли её: \"$negativeCondition\"?\n" +
                    "Оцени от 0 (вообще нет) до 100 (точно есть). Твой ответ должен состоять строго из одной цифры.\n" +
                    "Текст вакансии: ${text.take(1500)}"

        val result = runInference(prompt, systemPrompt, modelPath)
        val regex = Regex("\\d+")
        val match = regex.find(result)
        return match?.value?.toIntOrNull()?.coerceIn(0, 100) ?: 0
    }
}
