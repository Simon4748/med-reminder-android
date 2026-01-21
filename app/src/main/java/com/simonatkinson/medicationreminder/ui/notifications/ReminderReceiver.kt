package com.simonatkinson.medicationreminder.ui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Later we’ll read medication info from intent extras
        ReminderNotifier.showTestNotification(context)
    }
}
