package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1, // Singleton row
    val dailyGoalMl: Int = 2000,
    val reminderIntervalMinutes: Int = 60,
    val isNotificationsEnabled: Boolean = true,
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val isCloudSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
