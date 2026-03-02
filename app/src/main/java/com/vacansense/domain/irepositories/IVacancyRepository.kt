package com.vacansense.domain.irepositories

import com.vacansense.domain.models.HhFilters
import com.vacansense.domain.models.Vacancy
import com.vacansense.domain.models.VacancyStatus
import kotlinx.coroutines.flow.Flow

interface IVacancyRepository {
    suspend fun getNewVacanciesFromHH(query: String, filters: HhFilters): List<Vacancy>
    suspend fun getFullDescription(vacancyId: String): String
    suspend fun getUnprocessedVacancy(query: String): Vacancy?
    suspend fun saveVacancy(vacancy: Vacancy, query: String)
    suspend fun updateStatus(id: String, status: VacancyStatus, summary: String = "")
    suspend fun isVacancyExists(id: String): Boolean
    suspend fun resetProcessingToNew()
    fun getVacanciesForQueryFlow(query: String): Flow<List<Vacancy>>
    suspend fun deleteVacancy(id: String)
    suspend fun resetVacancyStatus(id: String)
}
