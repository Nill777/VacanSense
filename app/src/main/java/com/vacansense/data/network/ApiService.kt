package com.vacansense.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface HhApi {
    @GET("vacancies")
    suspend fun getVacancies(
        @Query("text") text: String,
        @Query("area") area: String? = "1",
        @Query("per_page") perPage: Int = 20,
        @Query("order_by") orderBy: String = "publication_time",
        @Query("period") period: Int? = null,
        @Query("experience") experience: String? = null,
        @Query("employment") employment: String? = null,
        @Query("schedule") schedule: String? = null,
        @Query("salary") salary: Int? = null
    ): Response<HhSearchResponse>

    @GET("vacancies/{id}")
    suspend fun getVacancyDetails(@Path("id") id: String): Response<HhDetailResponse>
}

// Data классы оставьте как были
data class HhSearchResponse(val items: List<HhItem>)
data class HhItem(
    val id: String,
    val name: String,
    val employer: HhEmployer?,
    val salary: HhSalary?,
    val alternate_url: String,
    val published_at: String
)

data class HhEmployer(val name: String)
data class HhSalary(val from: Int?, val to: Int?, val currency: String?)
data class HhDetailResponse(val description: String)

interface TelegramApi {
    @POST
    suspend fun sendMessage(
        @Url url: String,
        @Query("chat_id") chatId: String,
        @Query("text") text: String,
        @Query("parse_mode") parseMode: String = "HTML",
        @Query("disable_web_page_preview") disablePreview: Boolean = true
    ): Response<Unit>
}
