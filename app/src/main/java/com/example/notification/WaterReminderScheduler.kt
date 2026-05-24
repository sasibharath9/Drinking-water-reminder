package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock

class WaterReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun getPendingIntent(title: String, message: String): PendingIntent {
        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }
        return PendingIntent.getBroadcast(
            context,
            1234,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun schedulePeriodicReminders(intervalMinutes: Int) {
        cancelReminders()
        val intervalMillis = intervalMinutes * 60 * 1000L
        val triggerAtMillis = SystemClock.elapsedRealtime() + intervalMillis

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAtMillis,
                intervalMillis,
                getPendingIntent(
                    "Time to Hydrate! 💧",
                    "It has been $intervalMinutes minutes since your last reminder. Stay refreshed!"
                )
            )
        } catch (e: SecurityException) {
            // Safe fallback if SCHEDULE_EXACT_ALARM is required but unavailable
            alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAtMillis,
                intervalMillis,
                getPendingIntent(
                    "Time to Hydrate! 💧",
                    "It has been $intervalMinutes minutes since your last reminder. Stay refreshed!"
                )
            )
        }
    }

    fun scheduleDemoReminder(seconds: Int) {
        val triggerAtMillis = SystemClock.elapsedRealtime() + (seconds * 1000L)
        val pendingIntent = getPendingIntent(
            "Demo Hydration Reminder! 💧",
            "This is a quick preview reminder. Remember to meet your daily water goal!"
        )
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    fun cancelReminders() {
        val pendingIntent = getPendingIntent("", "")
        alarmManager.cancel(pendingIntent)
    }
}
