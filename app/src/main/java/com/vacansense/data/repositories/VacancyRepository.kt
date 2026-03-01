package com.vacansense.data.repositories

import com.vacansense.data.db.VacancyDao
import com.vacansense.data.db.VacancyEntity
import com.vacansense.data.network.HhApi
import com.vacansense.domain.irepositories.IVacancyRepository
import com.vacansense.domain.models.Vacancy

class VacancyRepository(
    private val api: HhApi,
    private val dao: VacancyDao
) : IVacancyRepository {

    override suspend fun getNewVacanciesFromHH(query: String): List<Vacancy> {
        return try {
            val response = api.getVacancies(text = query)
            response.body()?.items?.map { item ->
                val salaryStr = if (item.salary != null) {
                    "от ${item.salary.from ?: ""} до ${item.salary.to ?: ""} ${item.salary.currency}"
                } else "Не указана"

                Vacancy(
                    item.id,
                    item.name,
                    item.employer?.name ?: "",
                    salaryStr,
                    item.alternate_url,
                    item.published_at
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFullDescription(vacancyId: String): String {
        return try {
            val response = api.getVacancyDetails(vacancyId)
            response.body()?.description?.replace(Regex("<[^>]*>"), "") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun getUnprocessedVacancy(): Vacancy? {
        val entity = dao.getNew() ?: return null
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

    override suspend fun saveVacancy(vacancy: Vacancy) {
        dao.insert(
            VacancyEntity(
                vacancy.id,
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

    override suspend fun updateStatus(id: String, status: String, summary: String) {
        dao.updateStatus(id, status, summary)
    }

    override suspend fun isVacancyExists(id: String): Boolean {
        return dao.exists(id)
    }
}