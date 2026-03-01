package com.vacansense.domain.usecases

import com.vacansense.domain.irepositories.ILlmRepository
import com.vacansense.domain.irepositories.ITelegramRepository
import com.vacansense.domain.irepositories.IVacancyRepository

class ProcessVacanciesUseCase(
    private val vacancyRepo: IVacancyRepository,
    private val llmRepo: ILlmRepository,
    private val telegramRepo: ITelegramRepository
) {
    suspend fun execute(searchQuery: String, botToken: String, chatId: String, modelPath: String) {
        // 1. Парсим новые вакансии
        val newVacancies = vacancyRepo.getNewVacanciesFromHH(searchQuery)
        for (v in newVacancies) {
            if (!vacancyRepo.isVacancyExists(v.id)) {
                vacancyRepo.saveVacancy(v)
            }
        }

        // 2. Обрабатываем по одной
        val vacancyToProcess = vacancyRepo.getUnprocessedVacancy()
        if (vacancyToProcess != null) {
            vacancyRepo.updateStatus(vacancyToProcess.id, "PROCESSING")

            try {
                // Получаем полное описание
                val description = vacancyRepo.getFullDescription(vacancyToProcess.id)

                // Суммаризируем (самый долгий процесс)
                val summary = llmRepo.summarize(description, modelPath)

                // Формируем сообщение
                val msg = "🔥 <b>${vacancyToProcess.title}</b>\n" +
                        "🏢 <b>Компания:</b> ${vacancyToProcess.employer}\n" +
                        "💰 <b>ЗП:</b> ${vacancyToProcess.salary}\n\n" +
                        "🤖 <b>Выжимка AI:</b>\n$summary\n\n" +
                        "🔗 <a href='${vacancyToProcess.url}'>Ссылка на вакансию</a>"

                // Отправляем
                val isSent = telegramRepo.sendMessage(botToken, chatId, msg)

                if (isSent) {
                    vacancyRepo.updateStatus(vacancyToProcess.id, "DONE", summary)
                } else {
                    vacancyRepo.updateStatus(vacancyToProcess.id, "NEW")
                }
            } catch (e: Exception) {
                vacancyRepo.updateStatus(vacancyToProcess.id, "ERROR")
            }
        }
    }
}
