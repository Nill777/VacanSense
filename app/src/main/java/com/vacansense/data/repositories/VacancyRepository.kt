package com.vacansense.data.repositories

import android.util.Log
import com.vacansense.data.db.VacancyDao
import com.vacansense.data.db.VacancyEntity
import com.vacansense.data.network.HhApi
import com.vacansense.domain.irepositories.IVacancyRepository
import com.vacansense.domain.models.HhFilters
import com.vacansense.domain.models.Vacancy
import com.vacansense.domain.models.VacancyStatus
import kotlinx.coroutines.flow.map

class VacancyRepository(private val api: HhApi, private val dao: VacancyDao) : IVacancyRepository {
    override suspend fun getNewVacanciesFromHH(query: String, filters: HhFilters): List<Vacancy> {
        return try {
            val response = api.getVacancies(
                text = query,
                area = filters.area.takeIf { it.isNotBlank() },
                perPage = 20,
                orderBy = "publication_time",
                period = filters.period.takeIf { it > 0 },
                experience = filters.experience.takeIf { it.isNotBlank() },
                employment = filters.employment.takeIf { it.isNotBlank() },
                schedule = filters.schedule.takeIf { it.isNotBlank() },
                salary = filters.salary.takeIf { it > 0 }
            )
            response.body()?.items?.map { item ->
                val salaryStr =
                    if (item.salary != null) "от ${item.salary.from ?: ""} до ${item.salary.to ?: ""} ${item.salary.currency}" else "Не указана"
                Vacancy(
                    item.id,
                    item.name,
                    item.employer?.name ?: "",
                    salaryStr,
                    item.alternate_url,
                    item.published_at,
                    VacancyStatus.NEW
                )
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("VacancyRepo", "getNewVacanciesFromHH", e)
            emptyList()
        }
    }

    override suspend fun getFullDescription(vacancyId: String): String = try {
        val response = api.getVacancyDetails(vacancyId)
        response.body()?.description?.replace(Regex("<[^>]*>"), "") ?: ""
    } catch (e: Exception) {
        Log.e("VacancyRepo", "getFullDescription", e)
        ""
    }

    override suspend fun getUnprocessedVacancy(query: String): Vacancy? {
        val entity = dao.getByStatusAndQuery(query, VacancyStatus.NEW) ?: return null
        return Vacancy(
            entity.id,
            entity.title,
            entity.employer,
            entity.salary,
            entity.url,
            entity.publishedAt,
            entity.status,
            entity.summary
        )
    }

    override suspend fun saveVacancy(vacancy: Vacancy, query: String) {
        dao.insert(
            VacancyEntity(
                vacancy.id,
                query,
                vacancy.title,
                vacancy.employer,
                vacancy.salary,
                vacancy.url,
                vacancy.publishedAt,
                vacancy.status,
                vacancy.summary
            )
        )
    }

    override suspend fun updateStatus(id: String, status: VacancyStatus, summary: String) =
        dao.updateStatus(id, status, summary)

    override suspend fun isVacancyExists(id: String): Boolean = dao.exists(id)

    override suspend fun resetProcessingToNew() {
        dao.updateAllByStatus(VacancyStatus.PROCESSING, VacancyStatus.NEW)
    }

    override fun getVacanciesForQueryFlow(query: String) = dao.getByQueryFlow(query).map { list ->
        list.map {
            Vacancy(
                it.id,
                it.title,
                it.employer,
                it.salary,
                it.url,
                it.publishedAt,
                it.status,
                it.summary
            )
        }
    }

    override suspend fun deleteVacancy(id: String) = dao.deleteById(id)
    override suspend fun resetVacancyStatus(id: String) =
        dao.updateStatus(id, VacancyStatus.NEW, "")
}
