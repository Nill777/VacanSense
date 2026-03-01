package com.vacansense.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "vacancies")
data class VacancyEntity(
    @PrimaryKey val id: String,
    val queryContext: String,
    val title: String,
    val employer: String,
    val salary: String,
    val url: String,
    val publishedAt: String,
    val status: String,
    val summary: String
)

@Dao
interface VacancyDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vacancy: VacancyEntity)

    @Query("SELECT * FROM vacancies WHERE status = 'NEW' AND queryContext = :query LIMIT 1")
    suspend fun getNewForQuery(query: String): VacancyEntity?

    @Query("UPDATE vacancies SET status = :status, summary = :summary WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, summary: String)

    @Query("SELECT EXISTS(SELECT 1 FROM vacancies WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("SELECT * FROM vacancies WHERE queryContext = :query ORDER BY status DESC")
    fun getByQueryFlow(query: String): Flow<List<VacancyEntity>>

    @Query("DELETE FROM vacancies WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Database(entities = [VacancyEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vacancyDao(): VacancyDao
}
