package com.simonatkinson.medicationreminder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simonatkinson.medicationreminder.ui.medications.AddMedicationScreen
import com.simonatkinson.medicationreminder.ui.medications.MedicationListScreen

private object Routes {
    const val MEDICATION_LIST = "medication_list"
    const val ADD_MEDICATION = "add_medication"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MEDICATION_LIST
    ) {
        composable(Routes.MEDICATION_LIST) {
            MedicationListScreen(
                onAddMedication = { navController.navigate(Routes.ADD_MEDICATION) }
            )
        }
        composable(Routes.ADD_MEDICATION) {
            AddMedicationScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
