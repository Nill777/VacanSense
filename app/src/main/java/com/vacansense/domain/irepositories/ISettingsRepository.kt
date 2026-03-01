package com.vacansense.domain.irepositories

import com.vacansense.domain.models.AppSettings
import com.vacansense.domain.models.HhFilters
import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    val settingsFlow: Flow<AppSettings>
    val filtersFlow: Flow<HhFilters>

    suspend fun updateSettings(transform: suspend (AppSettings) -> AppSettings)
    suspend fun updateFilters(transform: suspend (HhFilters) -> HhFilters)
    suspend fun getSettings(): AppSettings
    suspend fun getFilters(): HhFilters
}
