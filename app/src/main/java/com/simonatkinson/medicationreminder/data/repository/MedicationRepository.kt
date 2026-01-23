package com.simonatkinson.medicationreminder.data.repository

import com.simonatkinson.medicationreminder.data.local.dao.MedicationWithSchedule
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun observeAll(): Flow<List<MedicationWithSchedule>>

    suspend fun upsertMedicationWithSchedule(
        id: String,
        name: String,
        doseAmount: String,
        doseUnit: String,
        notes: String,
        repeatEveryDay: Boolean,
        selectedDaysCsv: String,
        timesCsv: String
    )

    suspend fun deleteMedication(id: String)
}
