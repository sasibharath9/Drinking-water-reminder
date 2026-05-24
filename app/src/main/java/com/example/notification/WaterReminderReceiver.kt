package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class WaterReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WaterReminderReceiver", "Alarm received!")
        val notificationHelper = WaterNotificationHelper(context)
        
        val customTitle = intent.getStringExtra("title") ?: "Time to Hydrate! 💧"
        val customMessage = intent.getStringExtra("message") ?: "Keep your body healthy and refreshed. Drink a glass of water now!"
        
        notificationHelper.showHydrationReminder(customTitle, customMessage)
    }
}
