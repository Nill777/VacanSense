package com.vacansense.domain.usecases

import android.util.Log
import com.vacansense.domain.irepositories.ILlmRepository
import com.vacansense.domain.irepositories.ISettingsRepository
import com.vacansense.domain.irepositories.ITelegramRepository
import com.vacansense.domain.irepositories.IVacancyRepository
import com.vacansense.domain.models.VacancyStatus
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException

class ProcessVacanciesUseCase(
    private val vacancyRepo: IVacancyRepository,
    private val llmRepo: ILlmRepository,
    private val telegramRepo: ITelegramRepository,
    private val settingsRepo: ISettingsRepository
) {

    suspend fun resetProcessingToNew() {
        vacancyRepo.resetProcessingToNew()
    }

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

    suspend fun processNext(modelAbsolutePath: String): Boolean {
        val settings = settingsRepo.getSettings()
        if (settings.tgToken.isBlank() || settings.tgChatId.isBlank()) return false

        val vacancyToProcess = vacancyRepo.getUnprocessedVacancy(settings.query) ?: return false

        vacancyRepo.updateStatus(vacancyToProcess.id, VacancyStatus.PROCESSING)
        Log.d("UseCase", "Обработка вакансии: ${vacancyToProcess.title}")

        try {
            val description = vacancyRepo.getFullDescription(vacancyToProcess.id)

            if (settings.negativePrompt.isNotBlank()) {
                val negativeScore = llmRepo.evaluateNegative(description, settings.negativePrompt, modelAbsolutePath)
                if (negativeScore >= settings.negativeThreshold) {
                    vacancyRepo.updateStatus(vacancyToProcess.id, VacancyStatus.REJECTED, "Отклонено AI (скор: $negativeScore)")
                    return true
                }
            }

            val summary = llmRepo.summarize(description, settings.positivePrompt, modelAbsolutePath)

            val msg = "🔥 <b>${vacancyToProcess.title}</b>\n" +
                    "🏢 <b>Компания:</b> ${vacancyToProcess.employer}\n" +
                    "💰 <b>ЗП:</b> ${vacancyToProcess.salary}\n\n" +
                    "🤖 <b>Выжимка AI:</b>\n$summary\n\n" +
                    "🔗 <a href='${vacancyToProcess.url}'>Ссылка на вакансию</a>"

            val isSent = telegramRepo.sendMessage(settings.tgToken, settings.tgChatId, msg)

            if (isSent) {
                vacancyRepo.updateStatus(vacancyToProcess.id, VacancyStatus.DONE, summary)
            } else {
                vacancyRepo.updateStatus(vacancyToProcess.id, VacancyStatus.NEW)
            }

        } catch (e: CancellationException) {
            Log.w("UseCase", "Обработка отменена (Бот остановлен). Возвращаем статус NEW.")
            // withContext(NonCancellable) гарантирует, что запрос к БД выполнится даже при смерти корутины
            withContext(NonCancellable) {
                vacancyRepo.updateStatus(vacancyToProcess.id, VacancyStatus.NEW)
            }
            throw e // Пробрасываем дальше, чтобы корутина корректно завершилась
        } catch (e: Exception) {
            Log.e("UseCase", "Ошибка обработки", e)
            vacancyRepo.updateStatus(vacancyToProcess.id, VacancyStatus.ERROR)
        }

        return true
    }
}
