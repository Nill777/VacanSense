package com.vacansense.data.db

import androidx.room.TypeConverter
import com.vacansense.domain.models.VacancyStatus

class Converters {
    @TypeConverter
    fun fromStatus(status: VacancyStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(name: String): VacancyStatus {
        return try {
            VacancyStatus.valueOf(name)
        } catch (e: Exception) {
            VacancyStatus.NEW
        }
    }
}
