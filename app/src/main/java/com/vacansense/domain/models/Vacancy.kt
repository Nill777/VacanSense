package com.vacansense.domain.models

enum class VacancyStatus {
    NEW,
    PROCESSING,
    DONE,
    ERROR,
    REJECTED
}

data class Vacancy(
    val id: String,
    val title: String,
    val employer: String,
    val salary: String,
    val url: String,
    val publishedAt: String,
    val status: VacancyStatus = VacancyStatus.NEW,
    val summary: String = ""
)