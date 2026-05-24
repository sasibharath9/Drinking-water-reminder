package com.example.data.repository

import com.example.data.dao.WaterDao
import com.example.data.model.HydrationRecord
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class WaterRepository(private val waterDao: WaterDao) {

    val allRecords: Flow<List<HydrationRecord>> = waterDao.getAllRecords()

    val userSettingsFlow: Flow<UserSettings?> = waterDao.getUserSettingsFlow()

    fun getTodayRecordsFlow(): Flow<List<HydrationRecord>> {
        return waterDao.getTodayRecords(getStartOfTodayTimestamp())
    }

    suspend fun insertRecord(amountMl: Int) {
        val record = HydrationRecord(amountMl = amountMl)
        waterDao.insertRecord(record)
    }

    suspend fun deleteRecord(record: HydrationRecord) {
        waterDao.deleteRecord(record)
    }

    suspend fun clearAllRecords() {
        waterDao.deleteAllRecords()
    }

    suspend fun getUserSettings(): UserSettings {
        return waterDao.getUserSettings() ?: UserSettings().also {
            waterDao.saveUserSettings(it)
        }
    }

    suspend fun saveUserSettings(settings: UserSettings) {
        waterDao.saveUserSettings(settings)
    }

    private fun getStartOfTodayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
