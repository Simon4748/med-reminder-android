package com.simonatkinson.medicationreminder.ui.medications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.simonatkinson.medicationreminder.ui.notifications.ReminderNotifier
import androidx.compose.material3.OutlinedButton

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.simonatkinson.medicationreminder.ui.notifications.AlarmScheduler
import com.simonatkinson.medicationreminder.ui.notifications.ExactAlarmPermission





@Composable
fun MedicationListScreen(
    items: List<MedicationListItemUi>,
    onAddMedication: () -> Unit,
    onMedicationClick: (MedicationListItemUi) -> Unit = {}
) {
    if (items.isEmpty()) {
        MedicationListEmptyState(onAddMedication = onAddMedication)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Medications",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onAddMedication,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add medication")
        }

        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current

        val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) ReminderNotifier.showTestNotification(context)
        }


        OutlinedButton(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (granted) {
                        ReminderNotifier.showTestNotification(context)
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    ReminderNotifier.showTestNotification(context)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test reminder notification")
        }

        OutlinedButton(
            onClick = {
                val ctx = context
                if (!AlarmScheduler.canScheduleExactAlarms(ctx)) {
                    val intent: Intent? = ExactAlarmPermission.requestExactAlarmPermissionIntent(ctx)
                    if (intent != null) ctx.startActivity(intent)
                } else {
                    AlarmScheduler.scheduleExactOneMinuteTest(ctx)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Schedule exact reminder in 1 minute")
        }




        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items, key = { it.id }) { med ->
                MedicationRow(
                    item = med,
                    onClick = { onMedicationClick(med) }
                )
            }
        }
    }
}

@Composable
private fun MedicationRow(
    item: MedicationListItemUi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${item.dose} • ${item.scheduleSummary}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${item.daysSummary} • ${item.timesSummary}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (!item.nextDose.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.nextDose,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun MedicationListEmptyState(
    onAddMedication: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No medications yet",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Add your first medication to start reminders.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        Button(onClick = onAddMedication) {
            Text("Add medication")
        }
    }
}
