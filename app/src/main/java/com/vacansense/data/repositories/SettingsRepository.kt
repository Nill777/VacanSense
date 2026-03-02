package com.vacansense.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vacansense.domain.irepositories.ISettingsRepository
import com.vacansense.domain.models.AppSettings
import com.vacansense.domain.models.HhFilters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "vacansense_settings")

class SettingsRepository(private val context: Context) : ISettingsRepository {

    private object Keys {
        val QUERY = stringPreferencesKey("query")
        val TG_TOKEN = stringPreferencesKey("tg_token")
        val TG_CHAT_ID = stringPreferencesKey("tg_chat_id")
        val POSITIVE_PROMPT = stringPreferencesKey("positive_prompt")
        val NEGATIVE_PROMPT = stringPreferencesKey("negative_prompt")
        val THRESHOLD = intPreferencesKey("negative_threshold")
        val SELECTED_MODEL = stringPreferencesKey("selected_model")

        // Filters
        val AREA = stringPreferencesKey("filter_area")
        val EXPERIENCE = stringPreferencesKey("filter_experience")
        val EMPLOYMENT = stringPreferencesKey("filter_employment")
        val SCHEDULE = stringPreferencesKey("filter_schedule")
        val SALARY = intPreferencesKey("filter_salary")
        val PERIOD = intPreferencesKey("filter_period")
    }

    override val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            query = prefs[Keys.QUERY] ?: "Android разработчик",
            tgToken = prefs[Keys.TG_TOKEN] ?: "",
            tgChatId = prefs[Keys.TG_CHAT_ID] ?: "",
            positivePrompt = prefs[Keys.POSITIVE_PROMPT]
                ?: "Сделай короткое саммари этой вакансии на русском языке. Тезисно самое основное",
            negativePrompt = prefs[Keys.NEGATIVE_PROMPT] ?: "",
            negativeThreshold = prefs[Keys.THRESHOLD] ?: 50,
            selectedModelFileName = prefs[Keys.SELECTED_MODEL] ?: ""
        )
    }

    override val filtersFlow: Flow<HhFilters> = context.dataStore.data.map { prefs ->
        HhFilters(
            period = prefs[Keys.PERIOD] ?: 30,
            experience = prefs[Keys.EXPERIENCE] ?: "",
            employment = prefs[Keys.EMPLOYMENT] ?: "",
            schedule = prefs[Keys.SCHEDULE] ?: "",
            salary = prefs[Keys.SALARY] ?: 0,
            area = prefs[Keys.AREA] ?: "1"
        )
    }

    override suspend fun updateSettings(transform: suspend (AppSettings) -> AppSettings) {
        context.dataStore.edit { prefs ->
            val current = getSettings()
            val new = transform(current)
            prefs[Keys.QUERY] = new.query
            prefs[Keys.TG_TOKEN] = new.tgToken
            prefs[Keys.TG_CHAT_ID] = new.tgChatId
            prefs[Keys.POSITIVE_PROMPT] = new.positivePrompt
            prefs[Keys.NEGATIVE_PROMPT] = new.negativePrompt
            prefs[Keys.THRESHOLD] = new.negativeThreshold
            prefs[Keys.SELECTED_MODEL] = new.selectedModelFileName
        }
    }

    override suspend fun updateFilters(transform: suspend (HhFilters) -> HhFilters) {
        context.dataStore.edit { prefs ->
            val current = getFilters()
            val new = transform(current)
            prefs[Keys.AREA] = new.area
            prefs[Keys.EXPERIENCE] = new.experience
            prefs[Keys.EMPLOYMENT] = new.employment
            prefs[Keys.SCHEDULE] = new.schedule
            prefs[Keys.SALARY] = new.salary
            prefs[Keys.PERIOD] = new.period
        }
    }

    override suspend fun getSettings() = settingsFlow.first()
    override suspend fun getFilters() = filtersFlow.first()
}
