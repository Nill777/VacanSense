package com.vacansense.domain.irepositories

import com.vacansense.domain.models.Vacancy

interface IVacancyRepository {
    suspend fun getNewVacanciesFromHH(query: String): List<Vacancy>
    suspend fun getFullDescription(vacancyId: String): String
    suspend fun getUnprocessedVacancy(): Vacancy?
    suspend fun saveVacancy(vacancy: Vacancy)
    suspend fun updateStatus(id: String, status: String, summary: String = "")
    suspend fun isVacancyExists(id: String): Boolean
}
