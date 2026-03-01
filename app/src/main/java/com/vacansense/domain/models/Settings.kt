package com.vacansense.domain.models

data class AppSettings(
    val query: String = "Android разработчик",
    val tgToken: String = "",
    val tgChatId: String = "",
    val positivePrompt: String = "Сделай выжимку из этой вакансии. Напиши строго 3 пункта на русском языке без лишних слов:\n1. Стек технологий.\n2. Обязанности.\n3. Условия работы.",
    val negativePrompt: String = "",
    val negativeThreshold: Int = 80,
    val selectedModelFileName: String = ""
)
