package com.vacansense.domain.irepositories

import com.vacansense.domain.models.HhFilters
import com.vacansense.domain.models.Vacancy
import kotlinx.coroutines.flow.Flow

interface IVacancyRepository {
    suspend fun getNewVacanciesFromHH(query: String, filters: HhFilters): List<Vacancy>
    suspend fun getFullDescription(vacancyId: String): String
    suspend fun getUnprocessedVacancy(query: String): Vacancy?
    suspend fun saveVacancy(vacancy: Vacancy, query: String)
    suspend fun updateStatus(id: String, status: String, summary: String = "")
    suspend fun isVacancyExists(id: String): Boolean
    fun getVacanciesForQueryFlow(query: String): Flow<List<Vacancy>>
    suspend fun deleteVacancy(id: String)
    suspend fun resetVacancyStatus(id: String)
}
