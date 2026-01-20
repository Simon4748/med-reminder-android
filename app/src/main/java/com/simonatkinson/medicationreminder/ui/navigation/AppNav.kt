package com.simonatkinson.medicationreminder.ui.navigation

import androidx.compose.runtime.Composable
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
import java.time.LocalDateTime
import com.simonatkinson.medicationreminder.ui.medications.NextDoseCalculator


private object Routes {
    const val MEDICATION_LIST = "medication_list"
    const val ADD_MEDICATION = "add_medication"
    const val MEDICATION_DETAILS = "medication_details"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    var medications by remember { mutableStateOf(MedicationListDemoData.items) }

    // Selection + draft for edit
    var selectedId by remember { mutableStateOf<String?>(null) }
    var formDraft by remember { mutableStateOf<MedicationFormUi?>(null) }
    var editingId by remember { mutableStateOf<String?>(null) }

    fun selectedItem(): MedicationListItemUi? =
        selectedId?.let { id -> medications.firstOrNull { it.id == id } }

    NavHost(
        navController = navController,
        startDestination = Routes.MEDICATION_LIST
    ) {
        composable(Routes.MEDICATION_LIST) {
            MedicationListScreen(
                items = medications.map { med ->
                    med.copy(
                        nextDose = NextDoseCalculator.nextDoseLabel(
                            now = LocalDateTime.now(),
                            times = med.timesSummary.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            repeatEveryDay = med.daysSummary.equals("Every day", ignoreCase = true),
                            selectedDays = if (med.daysSummary.equals("Every day", ignoreCase = true)) {
                                emptySet()
                            } else {
                                med.daysSummary.split(",").map { it.trim() }.toSet()
                            }
                        )
                    )
                },

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
                // Fallback if selection disappeared, return to list
                navController.popBackStack()
            } else {
                MedicationDetailsScreen(
                    item = item,
                    onBack = { navController.popBackStack() },
                    onEdit = {
                        // Edit mode
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

                    val updatedItem = saved.toListItemUi(
                        id = id,
                        nextDose = if (isEdit) selectedItem()?.nextDose else null
                    )

                    medications = if (isEdit) {
                        medications.map { if (it.id == id) updatedItem else it }
                    } else {
                        medications + updatedItem
                    }

                    // Keep selection consistent
                    selectedId = id

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

    // For demo data
    val validDays = setOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
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
        // Preserve a consistent order for readability
        val order = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
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
