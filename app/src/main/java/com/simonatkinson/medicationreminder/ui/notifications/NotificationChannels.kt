package com.simonatkinson.medicationreminder.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REMINDERS = "reminders"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            REMINDERS,
            "Medication reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders you schedule for medications"
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}
