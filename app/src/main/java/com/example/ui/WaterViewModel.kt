package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.WaterDatabase
import com.example.data.model.HydrationRecord
import com.example.data.model.UserSettings
import com.example.data.repository.WaterRepository
import com.example.notification.WaterReminderScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class WaterViewModel(
    application: Application,
    private val repository: WaterRepository
) : AndroidViewModel(application) {

    private val scheduler = WaterReminderScheduler(application)

    val todayRecords: StateFlow<List<HydrationRecord>> = repository.getTodayRecordsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allRecords: StateFlow<List<HydrationRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userSettings: StateFlow<UserSettings> = repository.userSettingsFlow
        .combine(MutableStateFlow(UserSettings())) { databaseSettings, defaultSettings ->
            databaseSettings ?: defaultSettings
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    // Sync state for multi-device visual feedback
    private val _syncStatus = MutableStateFlow("Synced") // "Synced", "Syncing", "Offline Mode Enabled"
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    init {
        // Initial setup of reminder scheduler from saved settings
        viewModelScope.launch {
            val settings = repository.getUserSettings()
            if (settings.isNotificationsEnabled) {
                scheduler.schedulePeriodicReminders(settings.reminderIntervalMinutes)
            } else {
                scheduler.cancelReminders()
            }
        }
    }

    fun addWaterIntake(amountMl: Int) {
        viewModelScope.launch {
            _syncStatus.value = "Syncing"
            repository.insertRecord(amountMl)
            // Simulate device cloud syncing beautifully
            delay(1500)
            _syncStatus.value = "Synced"
        }
    }

    fun deleteWaterIntake(record: HydrationRecord) {
        viewModelScope.launch {
            _syncStatus.value = "Syncing"
            repository.deleteRecord(record)
            delay(1200)
            _syncStatus.value = "Synced"
        }
    }

    fun updateSettings(
        dailyGoalMl: Int,
        intervalMinutes: Int,
        notificationsEnabled: Boolean,
        themeMode: String,
        cloudSyncEnabled: Boolean
    ) {
        viewModelScope.launch {
            _syncStatus.value = "Syncing"
            val updated = UserSettings(
                dailyGoalMl = dailyGoalMl,
                reminderIntervalMinutes = intervalMinutes,
                isNotificationsEnabled = notificationsEnabled,
                themeMode = themeMode,
                isCloudSyncEnabled = cloudSyncEnabled,
                lastSyncTimestamp = System.currentTimeMillis()
            )
            repository.saveUserSettings(updated)

            if (notificationsEnabled) {
                scheduler.schedulePeriodicReminders(intervalMinutes)
            } else {
                scheduler.cancelReminders()
            }
            delay(1000)
            _syncStatus.value = if (cloudSyncEnabled) "Synced" else "Offline Mode Enabled"
        }
    }

    fun triggerQuickDemoReminder() {
        viewModelScope.launch {
            _syncStatus.value = "Preparing notification..."
            scheduler.scheduleDemoReminder(7) // trigger in 7 seconds for the user to try out!
            delay(1000)
            _syncStatus.value = if (userSettings.value.isCloudSyncEnabled) "Synced" else "Offline Mode"
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllRecords()
        }
    }

    // Helper functions for daily stats
    fun getTodayTotalMl(records: List<HydrationRecord>): Int {
        return records.sumOf { it.amountMl }
    }

    // Process weekly trends
    fun getWeeklyTrendData(records: List<HydrationRecord>): List<Pair<String, Int>> {
        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val calendar = Calendar.getInstance()
        
        // Let's create a map of day -> total water
        val weeklyTotals = mutableMapOf<Int, Int>() // Calendar.DAY_OF_WEEK -> Total ml
        for (i in Calendar.SUNDAY..Calendar.SATURDAY) {
            weeklyTotals[i] = 0
        }

        // Filter records from the last 7 days
        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.timeInMillis

        records.filter { it.timestamp >= sevenDaysAgo }.forEach { record ->
            calendar.timeInMillis = record.timestamp
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            weeklyTotals[dayOfWeek] = (weeklyTotals[dayOfWeek] ?: 0) + record.amountMl
        }

        // Now map to the correct strings starting from 6 days ago to today
        val result = ArrayList<Pair<String, Int>>()
        val currentDay = Calendar.getInstance()
        
        for (i in 6 downTo 0) {
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.DAY_OF_YEAR, -i)
            val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
            val dayName = when (dayOfWeek) {
                Calendar.SUNDAY -> "Sun"
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                else -> ""
            }
            result.add(Pair(dayName, weeklyTotals[dayOfWeek] ?: 0))
        }

        return result
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
                val database = WaterDatabase.getDatabase(application)
                val repository = WaterRepository(database.waterDao())
                @Suppress("UNCHECKED_CAST")
                return WaterViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
