package com.simonatkinson.medicationreminder.ui.medications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

private val DoseUnits = listOf("mg", "mcg", "mL", "units", "tablet(s)")

private enum class Frequency {
    ONCE_DAILY,
    TWICE_DAILY,
    CUSTOM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onBack: () -> Unit
) {
    // UI-only local state for now
    var name by remember { mutableStateOf("") }
    var doseAmount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var unitExpanded by remember { mutableStateOf(false) }
    var doseUnit by remember { mutableStateOf(DoseUnits.first()) }

    // Repeat pattern (Daily vs Specific days)
    var repeatEveryDay by remember { mutableStateOf(true) }
    val days = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
    var selectedDays by remember { mutableStateOf(setOf<String>()) }

    // Frequency (once/twice/custom)
    var frequency by remember { mutableStateOf(Frequency.ONCE_DAILY) }

    // Times (strings for UI phase)
    var times by remember { mutableStateOf(listOf("8:00 AM")) }

    // Time input dialog state (text mode)
    var showTimeDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var dialogInitHour by remember { mutableStateOf(8) }
    var dialogInitMinute by remember { mutableStateOf(0) }
    var dialogKey by remember { mutableStateOf(0) }

    // Scan disclaimer dialog state
    var showScanDisclaimer by remember { mutableStateOf(false) }

    fun formatTime(hour24: Int, minute: Int): String {
        val ampm = if (hour24 >= 12) "PM" else "AM"
        val hour12 = when (val hh = hour24 % 12) { 0 -> 12; else -> hh }
        val mm = minute.toString().padStart(2, '0')
        return "$hour12:$mm $ampm"
    }

    fun parseTimeOrDefault(time: String?): Pair<Int, Int> {
        if (time.isNullOrBlank()) return 8 to 0
        // Expected: "h:mm AM" or "h:mm PM"
        return try {
            val parts = time.trim().split(" ")
            val hm = parts[0].split(":")
            val h12 = hm[0].toInt()
            val m = hm[1].toInt()
            val ampm = parts.getOrNull(1)?.uppercase() ?: "AM"

            val h24 = when {
                ampm == "AM" && h12 == 12 -> 0
                ampm == "AM" -> h12
                ampm == "PM" && h12 == 12 -> 12
                else -> h12 + 12
            }
            h24 to m
        } catch (_: Exception) {
            8 to 0
        }
    }

    fun applyFrequencyDefaults(newFrequency: Frequency) {
        frequency = newFrequency
        times = when (newFrequency) {
            Frequency.ONCE_DAILY -> listOf("8:00 AM")
            Frequency.TWICE_DAILY -> listOf("8:00 AM", "8:00 PM")
            Frequency.CUSTOM -> listOf("8:00 AM")
        }
    }

    fun openTimeDialogForEdit(index: Int) {
        editingIndex = index
        val (h, m) = parseTimeOrDefault(times.getOrNull(index))
        dialogInitHour = h
        dialogInitMinute = m
        dialogKey += 1
        showTimeDialog = true
    }

    fun openTimeDialogForAdd() {
        editingIndex = null
        dialogInitHour = 8
        dialogInitMinute = 0
        dialogKey += 1
        showTimeDialog = true
    }

    // Validation
    val scheduleValid = repeatEveryDay || selectedDays.isNotEmpty()
    val timesValid = times.isNotEmpty()
    val canSave = name.isNotBlank() && doseAmount.isNotBlank() && scheduleValid && timesValid

    // Time input dialog
    if (showTimeDialog) {
        key(dialogKey) {
            val timeState = rememberTimePickerState(
                initialHour = dialogInitHour,
                initialMinute = dialogInitMinute,
                is24Hour = false
            )

            AlertDialog(
                onDismissRequest = { showTimeDialog = false },
                title = { Text("Select time") },
                text = {
                    TimeInput(state = timeState)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val picked = formatTime(timeState.hour, timeState.minute)
                            val idx = editingIndex
                            if (idx != null) {
                                // Replace in place
                                times = times.toMutableList().also { it[idx] = picked }
                            } else {
                                // Add new time
                                if (!times.contains(picked)) {
                                    times = times + picked
                                }
                            }
                            showTimeDialog = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimeDialog = false }) { Text("Cancel") }
                }
            )
        }
    }

    // Scan disclaimer dialog:
    if (showScanDisclaimer) {
        AlertDialog(
            onDismissRequest = { showScanDisclaimer = false },
            title = { Text("Before you scan") },
            text = {
                Text("Scanned text may be inaccurate. You’ll review and confirm all details before saving.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showScanDisclaimer = false
                        // TODO: Navigate to scan flow (camera screen) later
                    }
                ) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showScanDisclaimer = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Add medication",
            style = MaterialTheme.typography.headlineSmall
        )

        // Prominent scan button
        OutlinedButton(
            onClick = { showScanDisclaimer = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = "Scan"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan prescription label")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Medication name *") },
            placeholder = { Text("e.g., Metformin") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = doseAmount,
                onValueChange = { doseAmount = it },
                label = { Text("Dose *") },
                placeholder = { Text("e.g., 10") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.width(12.dp))

            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = !unitExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = doseUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unit") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                DropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false }
                ) {
                    DoseUnits.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = {
                                doseUnit = unit
                                unitExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            placeholder = { Text("e.g., Take with food") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Schedule",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Frequency",
            style = MaterialTheme.typography.bodyMedium
        )

        Column(modifier = Modifier.padding(top = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = frequency == Frequency.ONCE_DAILY,
                    onClick = { applyFrequencyDefaults(Frequency.ONCE_DAILY) }
                )
                Text("Once daily")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = frequency == Frequency.TWICE_DAILY,
                    onClick = { applyFrequencyDefaults(Frequency.TWICE_DAILY) }
                )
                Text("Twice daily")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = frequency == Frequency.CUSTOM,
                    onClick = { applyFrequencyDefaults(Frequency.CUSTOM) }
                )
                Text("Custom times")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Repeat",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                RadioButton(
                    selected = repeatEveryDay,
                    onClick = { repeatEveryDay = true }
                )
                Text("Daily")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = !repeatEveryDay,
                    onClick = { repeatEveryDay = false }
                )
                Text("Specific days")
            }
        }

        if (!repeatEveryDay) {
            Spacer(modifier = Modifier.height(8.dp))

            val firstRow = days.take(4)
            val secondRow = days.drop(4)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    firstRow.forEach { day ->
                        val selected = selectedDays.contains(day)
                        AssistChip(
                            onClick = {
                                selectedDays = if (selected) selectedDays - day else selectedDays + day
                            },
                            label = { Text(day) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    secondRow.forEach { day ->
                        val selected = selectedDays.contains(day)
                        AssistChip(
                            onClick = {
                                selectedDays = if (selected) selectedDays - day else selectedDays + day
                            },
                            label = { Text(day) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            if (selectedDays.isEmpty()) {
                Text(
                    text = "Select at least one day.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Times",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            times.forEachIndexed { index, time ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        tonalElevation = 1.dp,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { openTimeDialogForEdit(index) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = time,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "  (tap to edit)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (times.size > 1) {
                                times = times.toMutableList().also { it.removeAt(index) }
                            }
                        },
                        enabled = times.size > 1
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove time"
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { openTimeDialogForAdd() },
                modifier = Modifier.fillMaxWidth(),
                enabled = frequency == Frequency.CUSTOM
            ) {
                Text("Add time")
            }

            if (frequency != Frequency.CUSTOM) {
                Text(
                    text = "Switch to Custom times to add more.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    // Placeholder: save later
                },
                enabled = canSave
            ) {
                Text("Save")
            }
        }
    }
}
