package com.vacansense.domain.models

data class HhFilters(
    val period: Int = 30, // Дней
    val experience: String = "", // noExperience, between1And3, between3And6, moreThan6
    val employment: String = "", // full, part, project, volunteer, probation
    val schedule: String = "", // fullDay, shift, flexible, remote, flyInFlyOut
    val salary: Int = 0,
    val area: String = "113" // Москва по умолчанию
)
