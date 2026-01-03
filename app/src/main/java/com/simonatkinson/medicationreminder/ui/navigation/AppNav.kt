package com.simonatkinson.medicationreminder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simonatkinson.medicationreminder.ui.medications.AddMedicationScreen
import com.simonatkinson.medicationreminder.ui.medications.MedicationListScreen
import com.simonatkinson.medicationreminder.ui.medications.MedicationListDemoData
import com.simonatkinson.medicationreminder.ui.medications.MedicationListItemUi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.simonatkinson.medicationreminder.ui.medications.MedicationDetailsScreen

private object Routes {
    const val MEDICATION_LIST = "medication_list"
    const val ADD_MEDICATION = "add_medication"
    const val MEDICATION_DETAILS = "medication_details"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    var selected by remember { mutableStateOf<MedicationListItemUi?>(null) }

    NavHost(
        navController = navController,
        startDestination = Routes.MEDICATION_LIST
    ) {
        composable(Routes.MEDICATION_LIST) {
            MedicationListScreen(
                items = MedicationListDemoData.items,
                onAddMedication = { navController.navigate(Routes.ADD_MEDICATION) },
                onMedicationClick = {
                    selected = it
                    navController.navigate(Routes.MEDICATION_DETAILS)
                }
            )
        }
        composable(Routes.ADD_MEDICATION) {
            AddMedicationScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.MEDICATION_DETAILS) {
            val item = selected
            if (item == null) {
                // Fallback if user somehow navigates without a selection
                MedicationListScreen(
                    items = MedicationListDemoData.items,
                    onAddMedication = { navController.navigate(Routes.ADD_MEDICATION) }
                )
            } else {
                MedicationDetailsScreen(
                    item = item,
                    onBack = { navController.popBackStack() },
                    onEdit = {
                        // TODO next: open AddMedicationScreen in "edit mode"
                        navController.navigate(Routes.ADD_MEDICATION)
                    }
                )
            }
        }
    }
}
