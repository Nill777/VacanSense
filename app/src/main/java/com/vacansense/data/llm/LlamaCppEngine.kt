package com.vacansense.data.llm

import android.content.Context
import android.util.Log
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import com.vacansense.domain.irepositories.ILlmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LlamaCppEngine(private val context: Context) : ILlmRepository {

    override suspend fun summarize(text: String, modelPath: String): String = withContext(Dispatchers.IO) {
        Log.d("LlamaCppEngine", "Начинаю загрузку модели: $modelPath")

        var engine: InferenceEngine? = null

        try {
            // 1. Получаем экземпляр движка
            engine = AiChat.getInferenceEngine(context)

            // 2. Загружаем веса модели в оперативную память
            engine.loadModel(modelPath)
            Log.d("LlamaCppEngine", "Модель успешно загружена!")

            // 3. Задаем системный промпт (роль нейросети)
            engine.setSystemPrompt(
                "Ты опытный AI-помощник HR-специалиста. Твоя задача — делать очень краткие выжимки из текста вакансий."
            )

            // 4. Формируем пользовательский запрос
            val prompt = """
                Сделай выжимку из этой вакансии. Напиши строго 3 пункта на русском языке без лишних слов:
                1. Стек технологий.
                2. Обязанности.
                3. Условия работы.

                Текст вакансии:
                ${text.take(1500)}
            """.trimIndent()

            val stringBuilder = StringBuilder()

            // 5. Запускаем генерацию
            Log.d("LlamaCppEngine", "Генерация ответа...")
            engine.sendUserPrompt(prompt).collect { token ->
                stringBuilder.append(token)
            }

            val result = stringBuilder.toString().trim()
            Log.d("LlamaCppEngine", "Результат:\n$result")
            result

        } catch (e: Exception) {
            Log.e("LlamaCppEngine", "Ошибка в процессе инференса нейросети", e)
            "Ошибка генерации AI: ${e.localizedMessage}"
        } finally {
            // 6. Обязательная очистка ресурсов
            try {
                engine?.cleanUp()
                engine?.destroy()
                Log.d("LlamaCppEngine", "Память очищена, модель выгружена.")
            } catch (e: Exception) {
                Log.e("LlamaCppEngine", "Ошибка при очистке памяти", e)
            }
        }
    }
}