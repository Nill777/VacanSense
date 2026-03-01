package com.vacansense

import android.content.Context
import androidx.room.Room
import com.vacansense.data.db.AppDatabase
import com.vacansense.data.llm.LlamaCppEngine
import com.vacansense.data.network.HhApi
import com.vacansense.data.network.TelegramApi
import com.vacansense.data.utils.ModelDownloadManager
import com.vacansense.data.repositories.SettingsRepository
import com.vacansense.data.repositories.TelegramRepository
import com.vacansense.data.repositories.VacancyRepository
import com.vacansense.domain.usecases.ProcessVacanciesUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vacancies.db"
        ).fallbackToDestructiveMigration().build()
    }
    private val hhApi: HhApi by lazy {
        Retrofit.Builder().baseUrl("https://api.hh.ru/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(HhApi::class.java)
    }
    private val telegramApi: TelegramApi by lazy {
        Retrofit.Builder().baseUrl("https://api.telegram.org/")
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(TelegramApi::class.java)
    }

    val vacancyRepository by lazy { VacancyRepository(hhApi, database.vacancyDao()) }
    val telegramRepository by lazy { TelegramRepository(telegramApi) }
    val llmRepository by lazy { LlamaCppEngine(context) }
    val settingsRepository by lazy { SettingsRepository(context) }
    val modelDownloadManager by lazy { ModelDownloadManager(context) }

    val processVacanciesUseCase by lazy {
        ProcessVacanciesUseCase(
            vacancyRepository,
            llmRepository,
            telegramRepository,
            settingsRepository
        )
    }
}
