package com.vacansense

import android.content.Context
import androidx.room.Room
import com.vacansense.data.db.AppDatabase
import com.vacansense.data.llm.LlamaCppEngine
import com.vacansense.data.network.HhApi
import com.vacansense.data.network.TelegramApi
import com.vacansense.data.repositories.TelegramRepository
import com.vacansense.data.repositories.VacancyRepository
import com.vacansense.domain.irepositories.ILlmRepository
import com.vacansense.domain.irepositories.ITelegramRepository
import com.vacansense.domain.irepositories.IVacancyRepository
import com.vacansense.domain.usecases.ProcessVacanciesUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Контейнер для Manual DI (Ручное внедрение зависимостей).
 * Инициализируется один раз при старте Application.
 */
class AppContainer(context: Context) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "vacancies.db").build()
    }

    private val hhApi: HhApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.hh.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HhApi::class.java)
    }

    private val telegramApi: TelegramApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.telegram.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TelegramApi::class.java)
    }

    private val vacancyRepository: IVacancyRepository by lazy {
        VacancyRepository(hhApi, database.vacancyDao())
    }

    private val telegramRepository: ITelegramRepository by lazy {
        TelegramRepository(telegramApi)
    }

    private val llmRepository: ILlmRepository by lazy {
        LlamaCppEngine(context)
    }

    val processVacanciesUseCase: ProcessVacanciesUseCase by lazy {
        ProcessVacanciesUseCase(vacancyRepository, llmRepository, telegramRepository)
    }
}
