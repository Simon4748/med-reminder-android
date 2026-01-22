package com.simonatkinson.medicationreminder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simonatkinson.medicationreminder.ui.medications.AddMedicationScreen
import com.simonatkinson.medicationreminder.ui.medications.FrequencyUi
import com.simonatkinson.medicationreminder.ui.medications.MedicationDetailsScreen
import com.simonatkinson.medicationreminder.ui.medications.MedicationFormUi
import com.simonatkinson.medicationreminder.ui.medications.MedicationListDemoData
import com.simonatkinson.medicationreminder.ui.medications.MedicationListItemUi
import com.simonatkinson.medicationreminder.ui.medications.MedicationListScreen
import com.simonatkinson.medicationreminder.ui.medications.NextDoseCalculator
import kotlinx.coroutines.delay
import java.time.LocalDateTime

private object Routes {
    const val MEDICATION_LIST = "medication_list"
    const val ADD_MEDICATION = "add_medication"
    const val MEDICATION_DETAILS = "medication_details"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    // Single source of truth
    var medications by remember { mutableStateOf(MedicationListDemoData.items) }

    // Selection + draft for edit
    var selectedId by remember { mutableStateOf<String?>(null) }
    var formDraft by remember { mutableStateOf<MedicationFormUi?>(null) }
    var editingId by remember { mutableStateOf<String?>(null) } // null = Add, non-null = Edit

    // Clock tick for next dose
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    // Context used for scheduling alarms
    val context = androidx.compose.ui.platform.LocalContext.current

    var showExactAlarmDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(30_000) // update twice a minute
        }
    }

    fun selectedItem(): MedicationListItemUi? =
        selectedId?.let { id -> medications.firstOrNull { it.id == id } }

    if (showExactAlarmDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExactAlarmDialog = false },
            title = { androidx.compose.material3.Text("Enable exact reminders") },
            text = {
                androidx.compose.material3.Text(
                    "To deliver reminders at precise times, Android requires enabling “Alarms & reminders” for this app."
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showExactAlarmDialog = false
                        val intent = com.simonatkinson.medicationreminder.ui.notifications.ExactAlarmPermission
                            .requestExactAlarmPermissionIntent(context)
                        if (intent != null) context.startActivity(intent)
                    }
                ) { androidx.compose.material3.Text("Enable") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showExactAlarmDialog = false }) {
                    androidx.compose.material3.Text("Not now")
                }
            }
        )
    }


    NavHost(
        navController = navController,
        startDestination = Routes.MEDICATION_LIST
    ) {
        composable(Routes.MEDICATION_LIST) {
            val computed = medications.map { med ->
                med.copy(
                    nextDose = NextDoseCalculator.nextDoseLabel(
                        now = now,
                        times = med.timesSummary
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() },
                        repeatEveryDay = med.daysSummary.equals("Every day", ignoreCase = true),
                        selectedDays = if (med.daysSummary.equals("Every day", ignoreCase = true)) {
                            emptySet()
                        } else {
                            med.daysSummary.split(",").map { it.trim() }.toSet()
                        }
                    )
                )
            }

            MedicationListScreen(
                items = computed,
                onAddMedication = {
                    // Add mode
                    formDraft = null
                    editingId = null
                    navController.navigate(Routes.ADD_MEDICATION)
                },
                onMedicationClick = { item ->
                    selectedId = item.id
                    navController.navigate(Routes.MEDICATION_DETAILS)
                }
            )
        }

        composable(Routes.MEDICATION_DETAILS) {
            val item = selectedItem()
            if (item == null) {
                navController.popBackStack()
            } else {
                MedicationDetailsScreen(
                    item = item,
                    onBack = { navController.popBackStack() },
                    onEdit = {
                        editingId = item.id
                        formDraft = item.toFormDraft()
                        navController.navigate(Routes.ADD_MEDICATION)
                    }
                )
            }
        }

        composable(Routes.ADD_MEDICATION) {
            val isEdit = editingId != null

            AddMedicationScreen(
                onBack = { navController.popBackStack() },
                modeTitle = if (isEdit) "Edit medication" else "Add medication",
                initial = formDraft ?: MedicationFormUi(),
                onSave = { saved ->
                    val id = editingId ?: generateId(saved.name)

                    // Capture old times before update medications
                    val oldTimes: List<String> = if (isEdit) {
                        medications.firstOrNull { it.id == id }
                            ?.timesSummary
                            ?.split(",")
                            ?.map { it.trim() }
                            ?.filter { it.isNotBlank() }
                            .orEmpty()
                    } else {
                        emptyList()
                    }

                    val updatedItem = saved.toListItemUi(
                        id = id,
                        nextDose = if (isEdit) selectedItem()?.nextDose else null
                    )

                    // Update in-memory list
                    medications = if (isEdit) {
                        medications.map { if (it.id == id) updatedItem else it }
                    } else {
                        medications + updatedItem
                    }

                    // Keep selection consistent
                    selectedId = id

                    // ---- Notifications / alarms ---

                    // Cancel old alarms for that medication/time slots
                    if (isEdit) {
                        oldTimes.forEach { slot ->
                            com.simonatkinson.medicationreminder.ui.notifications.AlarmScheduler.cancelTimeSlot(
                                context = context,
                                medId = id,
                                timeSlot = slot
                            )
                        }
                    }

                    // Schedule new alarms
                    if (com.simonatkinson.medicationreminder.ui.notifications.AlarmScheduler.canScheduleExactAlarms(context)) {
                        val nowTs = java.time.LocalDateTime.now()
                        val doseText = "${saved.doseAmount} ${saved.doseUnit}".trim()

                        saved.times.map { it.trim() }
                            .filter { it.isNotBlank() }
                            .forEach { slot ->
                                val next = com.simonatkinson.medicationreminder.ui.medications.NextDoseCalculator.nextOccurrenceForTimeSlot(
                                    now = nowTs,
                                    timeText = slot,
                                    repeatEveryDay = saved.repeatEveryDay,
                                    selectedDays = saved.selectedDays
                                ) ?: return@forEach

                                com.simonatkinson.medicationreminder.ui.notifications.AlarmScheduler.scheduleExactTimeSlot(
                                    context = context,
                                    medId = id,
                                    medName = saved.name,
                                    doseText = doseText,
                                    timeSlot = slot,
                                    repeatEveryDay = saved.repeatEveryDay,
                                    selectedDays = saved.selectedDays,
                                    triggerAtMillis = next.atZone(java.time.ZoneId.systemDefault())
                                        .toInstant()
                                        .toEpochMilli()
                                )
                            }
                    } else {
                        showExactAlarmDialog = true
                    }

                    // Clear edit state last
                    formDraft = null
                    editingId = null
                }
            )
        }
    }
}

/** UI-only mapping: list item -> form draft (for prefilling Edit) */
private fun MedicationListItemUi.toFormDraft(): MedicationFormUi {
    val doseParts = dose.trim().split(" ", limit = 2)
    val amount = doseParts.getOrNull(0).orEmpty()
    val unit = doseParts.getOrNull(1)?.trim().orEmpty().ifBlank { "mg" }

    val freq = when {
        scheduleSummary.contains("Twice", ignoreCase = true) -> FrequencyUi.TWICE_DAILY
        scheduleSummary.contains("Once", ignoreCase = true) -> FrequencyUi.ONCE_DAILY
        else -> FrequencyUi.CUSTOM
    }

    val timesList = timesSummary
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .ifEmpty { listOf("8:00 AM") }

    val everyDay = daysSummary.equals("Every day", ignoreCase = true)

    val validDays = setOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val daySet = if (everyDay) emptySet() else {
        daysSummary.split(",")
            .map { it.trim() }
            .filter { it in validDays }
            .toSet()
    }

    return MedicationFormUi(
        name = name,
        doseAmount = amount,
        doseUnit = unit,
        notes = "",
        frequency = freq,
        repeatEveryDay = everyDay,
        selectedDays = daySet,
        times = timesList
    )
}

/** UI-only mapping: form state -> list row */
private fun MedicationFormUi.toListItemUi(
    id: String,
    nextDose: String? = null
): MedicationListItemUi {
    val schedule = when (frequency) {
        FrequencyUi.ONCE_DAILY -> "Once daily"
        FrequencyUi.TWICE_DAILY -> "Twice daily"
        FrequencyUi.CUSTOM -> "Custom times"
    }

    val days = if (repeatEveryDay) {
        "Every day"
    } else {
        val order = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        order.filter { selectedDays.contains(it) }.joinToString(", ").ifBlank { "Specific days" }
    }

    val timesText = times.joinToString(", ")

    return MedicationListItemUi(
        id = id,
        name = name,
        dose = "${doseAmount.trim()} ${doseUnit.trim()}".trim(),
        scheduleSummary = schedule,
        daysSummary = days,
        timesSummary = timesText,
        nextDose = nextDose
    )
}

private fun generateId(name: String): String {
    val base = name.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
    return if (base.isBlank()) "med-${System.currentTimeMillis()}" else "$base-${System.currentTimeMillis()}"
}
