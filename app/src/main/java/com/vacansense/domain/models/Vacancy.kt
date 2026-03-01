package com.vacansense.domain.models

data class Vacancy(
    val id: String,
    val title: String,
    val employer: String,
    val salary: String,
    val url: String,
    val publishedAt: String,
    val status: String = "NEW", // NEW, PROCESSING, DONE, ERROR
    val summary: String = ""
)