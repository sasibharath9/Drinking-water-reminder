package com.example.data.dao

import androidx.room.*
import com.example.data.model.HydrationRecord
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    // ---- Hydration Records ----
    @Query("SELECT * FROM hydration_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<HydrationRecord>>

    @Query("SELECT * FROM hydration_records WHERE timestamp >= :startOfDayTimestamp ORDER BY timestamp ASC")
    fun getTodayRecords(startOfDayTimestamp: Long): Flow<List<HydrationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HydrationRecord): Long

    @Delete
    suspend fun deleteRecord(record: HydrationRecord)

    @Query("DELETE FROM hydration_records")
    suspend fun deleteAllRecords()

    // ---- User Settings ----
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettingsFlow(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettings(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettings)
}
