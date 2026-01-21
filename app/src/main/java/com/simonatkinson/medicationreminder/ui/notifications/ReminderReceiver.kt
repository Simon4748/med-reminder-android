package com.simonatkinson.medicationreminder.ui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import com.simonatkinson.medicationreminder.ui.medications.NextDoseCalculator

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getStringExtra(EXTRA_MED_ID) ?: return
        val medName = intent.getStringExtra(EXTRA_MED_NAME) ?: "Medication"
        val doseText = intent.getStringExtra(EXTRA_DOSE_TEXT) ?: ""
        val timeSlot = intent.getStringExtra(EXTRA_TIME_SLOT) ?: return

        val repeatEveryDay = intent.getBooleanExtra(EXTRA_REPEAT_EVERY_DAY, true)
        val selectedDaysCsv = intent.getStringExtra(EXTRA_SELECTED_DAYS_CSV).orEmpty()
        val selectedDays = selectedDaysCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

        // 1) Show notification for this slot
        ReminderNotifier.showMedicationReminder(
            context = context,
            medicationName = medName,
            doseText = doseText,
            timeText = timeSlot
        )

        // 2) Chain schedule the NEXT occurrence for the same slot
        val next = NextDoseCalculator.nextOccurrenceForTimeSlot(
            now = LocalDateTime.now(),
            timeText = timeSlot,
            repeatEveryDay = repeatEveryDay,
            selectedDays = selectedDays
        ) ?: return

        AlarmScheduler.scheduleExactTimeSlot(
            context = context,
            medId = medId,
            medName = medName,
            doseText = doseText,
            timeSlot = timeSlot,
            repeatEveryDay = repeatEveryDay,
            selectedDays = selectedDays,
            triggerAtMillis = next.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }

    companion object {
        const val EXTRA_MED_ID = "extra_med_id"
        const val EXTRA_MED_NAME = "extra_med_name"
        const val EXTRA_DOSE_TEXT = "extra_dose_text"
        const val EXTRA_TIME_SLOT = "extra_time_slot"
        const val EXTRA_REPEAT_EVERY_DAY = "extra_repeat_every_day"
        const val EXTRA_SELECTED_DAYS_CSV = "extra_selected_days_csv"
    }
}
