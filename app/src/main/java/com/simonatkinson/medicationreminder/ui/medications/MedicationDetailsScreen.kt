package com.simonatkinson.medicationreminder.ui.medications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MedicationDetailsScreen(
    item: MedicationListItemUi,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Button(onClick = onEdit) { Text("Edit") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(item.name, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        Text("Dose", style = MaterialTheme.typography.titleMedium)
        Text(item.dose, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(12.dp))

        Text("Schedule", style = MaterialTheme.typography.titleMedium)
        Text(item.scheduleSummary, style = MaterialTheme.typography.bodyLarge)
        Text("${item.daysSummary} • ${item.timesSummary}", style = MaterialTheme.typography.bodyMedium)

        if (!item.nextDose.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Next reminder", style = MaterialTheme.typography.titleMedium)
            Text(item.nextDose, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
