package com.simonatkinson.medicationreminder.ui.medications

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object NextDoseCalculator {

    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    /**
     * times list like "8:00 AM", "8:00 PM"
     * repeatEveryDay: true => every day, false => only on selectedDays like "Mon","Wed"
     */
    fun nextDoseLabel(
        now: LocalDateTime,
        times: List<String>,
        repeatEveryDay: Boolean,
        selectedDays: Set<String>
    ): String? {
        val parsedTimes = times.mapNotNull { parseTime(it) }.sorted()
        if (parsedTimes.isEmpty()) return null

        val allowedDays = if (repeatEveryDay) null else selectedDaysToDayOfWeek(selectedDays)
        if (!repeatEveryDay && allowedDays.isNullOrEmpty()) return null

        // Search up to 8 days ahead
        for (dayOffset in 0..8) {
            val date = now.toLocalDate().plusDays(dayOffset.toLong())
            val dow = date.dayOfWeek

            if (allowedDays != null && dow !in allowedDays) continue

            val candidate = if (dayOffset == 0) {
                // Today, only times after now
                parsedTimes.firstOrNull { it.isAfter(now.toLocalTime()) }
            } else {
                // Future day, earliest time
                parsedTimes.first()
            }

            if (candidate != null) {
                val whenText = humanDate(now.toLocalDate(), date)
                val timeText = candidate.format(timeFormatter)
                return "Next: $whenText $timeText"
            }
        }

        return null
    }

    private fun parseTime(text: String): LocalTime? {
        return try {
            LocalTime.parse(text.trim().uppercase(Locale.US), timeFormatter)
        } catch (_: Exception) {
            null
        }
    }

    private fun selectedDaysToDayOfWeek(selected: Set<String>): Set<DayOfWeek> {
        return selected.mapNotNull { abbrev ->
            when (abbrev.trim()) {
                "Mon" -> DayOfWeek.MONDAY
                "Tue" -> DayOfWeek.TUESDAY
                "Wed" -> DayOfWeek.WEDNESDAY
                "Thu" -> DayOfWeek.THURSDAY
                "Fri" -> DayOfWeek.FRIDAY
                "Sat" -> DayOfWeek.SATURDAY
                "Sun" -> DayOfWeek.SUNDAY
                else -> null
            }
        }.toSet()
    }

    private fun humanDate(today: LocalDate, date: LocalDate): String {
        return when (date) {
            today -> "Today"
            today.plusDays(1) -> "Tomorrow"
            else -> date.dayOfWeek.name.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
