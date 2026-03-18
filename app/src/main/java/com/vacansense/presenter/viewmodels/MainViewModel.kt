package com.vacansense.presenter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vacansense.data.utils.ModelDownloadManager
import com.vacansense.domain.irepositories.ISettingsRepository
import com.vacansense.domain.irepositories.IVacancyRepository
import com.vacansense.domain.models.AiModel
import com.vacansense.domain.models.AppSettings
import com.vacansense.domain.models.HhFilters
import com.vacansense.domain.models.Vacancy
import com.vacansense.domain.utils.BotSignalManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val settingsRepo: ISettingsRepository,
    private val downloadManager: ModelDownloadManager,
    private val vacancyRepo: IVacancyRepository,
    private val botSignalManager: BotSignalManager
) : ViewModel() {

    val appSettings: StateFlow<AppSettings> = settingsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val hhFilters: StateFlow<HhFilters> = settingsRepo.filtersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HhFilters())

    val modelsState: StateFlow<List<AiModel>> = downloadManager.modelsState

    val currentVacancies: StateFlow<List<Vacancy>> = appSettings
        .map { it.query }
        .distinctUntilChanged()
        .flatMapLatest { q -> vacancyRepo.getVacanciesForQueryFlow(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    init {
        viewModelScope.launch {
            downloadManager.observeDownloads()
        }
    }

    fun updateSetting(updater: suspend (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsRepo.updateSettings(updater) }
    }

    fun updateFilters(updater: suspend (HhFilters) -> HhFilters) {
        viewModelScope.launch { settingsRepo.updateFilters(updater) }
    }

    fun setRunning(state: Boolean) {
        _isRunning.value = state
    }

    fun downloadModel(model: AiModel) {
        downloadManager.downloadModel(model)
    }

    fun selectModel(fileName: String) {
        updateSetting { it.copy(selectedModelFileName = fileName) }
    }

    fun deleteModel(fileName: String) {
        downloadManager.deleteModel(fileName)
        if (appSettings.value.selectedModelFileName == fileName) {
            updateSetting { it.copy(selectedModelFileName = "") }
        }
    }

    fun resetVacancy(id: String) {
        viewModelScope.launch {
            vacancyRepo.resetVacancyStatus(id)
            botSignalManager.wakeUp()
        }
    }

    fun deleteVacancy(id: String) {
        viewModelScope.launch { vacancyRepo.deleteVacancy(id) }
    }

    class Factory(
        private val settingsRepo: ISettingsRepository,
        private val downloadManager: ModelDownloadManager,
        private val vacancyRepo: IVacancyRepository,
        private val botSignalManager: BotSignalManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel(settingsRepo, downloadManager, vacancyRepo, botSignalManager) as T
    }
}
