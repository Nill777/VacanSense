package com.vacansense.domain.usecases

import android.util.Log
import com.vacansense.domain.irepositories.ILlmRepository
import com.vacansense.domain.irepositories.ISettingsRepository
import com.vacansense.domain.irepositories.ITelegramRepository
import com.vacansense.domain.irepositories.IVacancyRepository

class ProcessVacanciesUseCase(
    private val vacancyRepo: IVacancyRepository,
    private val llmRepo: ILlmRepository,
    private val telegramRepo: ITelegramRepository,
    private val settingsRepo: ISettingsRepository
) {

    /* Шаг 1: Ищем новые вакансии на HH и сохраняем в БД.
    * Не запускает AI, только наполняет базу.
    */
    suspend fun fetchAndSave() {
        val settings = settingsRepo.getSettings()
        val filters = settingsRepo.getFilters()

        if (settings.query.isBlank()) return

        Log.d("UseCase", "Запрос к HH: ${settings.query}")
        val newVacancies = vacancyRepo.getNewVacanciesFromHH(settings.query, filters)
        var count = 0
        for (v in newVacancies) {
            if (!vacancyRepo.isVacancyExists(v.id)) {
                vacancyRepo.saveVacancy(v, settings.query)
                count++
            }
        }
        Log.d("UseCase", "Сохранено новых вакансий: $count")
    }

    /* Шаг 2: Берет ОДНУ необработанную вакансию из базы и прогоняет через AI.
    * @return true - если вакансия была найдена и обработана (успешно или нет).
    * @return false - если необработанных вакансий больше нет.
    */
    suspend fun processNext(modelAbsolutePath: String): Boolean {
        val settings = settingsRepo.getSettings()
        if (settings.tgToken.isBlank() || settings.tgChatId.isBlank()) {
            Log.e("UseCase", "Нет токенов Telegram")
            return false
        }

        // Берем первую попавшуюся со статусом NEW
        val vacancyToProcess = vacancyRepo.getUnprocessedVacancy(settings.query) ?: return false

        // Ставим статус "В процессе", чтобы другие потоки (если будут) не взяли её
        vacancyRepo.updateStatus(vacancyToProcess.id, "PROCESSING")
        Log.d("UseCase", "Обработка вакансии: ${vacancyToProcess.title}")

        try {
            val description = vacancyRepo.getFullDescription(vacancyToProcess.id)

            // 1. Проверка негативного промта (если задан)
            if (settings.negativePrompt.isNotBlank()) {
                val negativeScore = llmRepo.evaluateNegative(
                    description,
                    settings.negativePrompt,
                    modelAbsolutePath
                )
                if (negativeScore >= settings.negativeThreshold) {
                    vacancyRepo.updateStatus(
                        vacancyToProcess.id,
                        "REJECTED",
                        "Отклонено AI (содержит негативные маркеры, оценка: $negativeScore)"
                    )
                    Log.d("UseCase", "Вакансия отклонена AI")
                    return true // Мы обработали вакансию (отклонили), возвращаем true
                }
            }

            // 2. Суммаризация (Положительный промт)
            val summary = llmRepo.summarize(description, settings.positivePrompt, modelAbsolutePath)

            // 3. Отправка в Telegram
            val msg = "🔥 <b>${vacancyToProcess.title}</b>\n" +
                    "🏢 <b>Компания:</b> ${vacancyToProcess.employer}\n" +
                    "💰 <b>ЗП:</b> ${vacancyToProcess.salary}\n\n" +
                    "🤖 <b>Выжимка AI:</b>\n$summary\n\n" +
                    "🔗 <a href='${vacancyToProcess.url}'>Ссылка на вакансию</a>"

            val isSent = telegramRepo.sendMessage(settings.tgToken, settings.tgChatId, msg)

            if (isSent) {
                vacancyRepo.updateStatus(vacancyToProcess.id, "DONE", summary)
                Log.d("UseCase", "Вакансия отправлена в Telegram")
            } else {
                // Если ошибка сети телеграма, возвращаем в NEW, чтобы попробовать позже
                vacancyRepo.updateStatus(vacancyToProcess.id, "NEW")
                Log.e("UseCase", "Ошибка отправки в Telegram")
            }
        } catch (e: Exception) {
            Log.e("UseCase", "Ошибка обработки", e)
            vacancyRepo.updateStatus(vacancyToProcess.id, "ERROR")
        }

        return true // Вакансия была обработана
    }
}
