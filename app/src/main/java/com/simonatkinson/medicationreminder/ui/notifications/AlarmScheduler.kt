package com.simonatkinson.medicationreminder.ui.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmScheduler {

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    fun scheduleExactTimeSlot(
        context: Context,
        medId: String,
        medName: String,
        doseText: String,
        timeSlot: String,
        repeatEveryDay: Boolean,
        selectedDays: Set<String>,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(
            context = context,
            medId = medId,
            medName = medName,
            doseText = doseText,
            timeSlot = timeSlot,
            repeatEveryDay = repeatEveryDay,
            selectedDays = selectedDays
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    fun cancelTimeSlot(
        context: Context,
        medId: String,
        timeSlot: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeFor(medId, timeSlot),
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun buildPendingIntent(
        context: Context,
        medId: String,
        medName: String,
        doseText: String,
        timeSlot: String,
        repeatEveryDay: Boolean,
        selectedDays: Set<String>
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_MED_ID, medId)
            putExtra(ReminderReceiver.EXTRA_MED_NAME, medName)
            putExtra(ReminderReceiver.EXTRA_DOSE_TEXT, doseText)
            putExtra(ReminderReceiver.EXTRA_TIME_SLOT, timeSlot)
            putExtra(ReminderReceiver.EXTRA_REPEAT_EVERY_DAY, repeatEveryDay)
            putExtra(ReminderReceiver.EXTRA_SELECTED_DAYS_CSV, selectedDays.joinToString(","))
        }

        return PendingIntent.getBroadcast(
            context,
            requestCodeFor(medId, timeSlot),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun requestCodeFor(medId: String, timeSlot: String): Int {
        return (medId + "|" + timeSlot).hashCode()
    }

    fun scheduleExactOneMinuteTest(context: Context) {
        val triggerAtMillis = System.currentTimeMillis() + 60_000L

        scheduleExactTimeSlot(
            context = context,
            medId = "test-med",
            medName = "Test medication",
            doseText = "Test dose",
            timeSlot = "Test time",
            repeatEveryDay = true,
            selectedDays = emptySet(),
            triggerAtMillis = triggerAtMillis
        )
    }

}
