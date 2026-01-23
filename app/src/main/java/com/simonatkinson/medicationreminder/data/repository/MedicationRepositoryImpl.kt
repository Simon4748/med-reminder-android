package com.simonatkinson.medicationreminder.data.repository

import android.content.Context
import com.simonatkinson.medicationreminder.data.local.AppDatabase
import com.simonatkinson.medicationreminder.data.local.entities.MedicationEntity
import com.simonatkinson.medicationreminder.data.local.entities.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import com.simonatkinson.medicationreminder.data.local.dao.MedicationWithSchedule

class MedicationRepositoryImpl(context: Context) : MedicationRepository {

    private val dao = AppDatabase.getInstance(context).medicationDao()

    override fun observeAll(): Flow<List<MedicationWithSchedule>> = dao.observeAllWithSchedule()

    override suspend fun upsertMedicationWithSchedule(
        id: String,
        name: String,
        doseAmount: String,
        doseUnit: String,
        notes: String,
        repeatEveryDay: Boolean,
        selectedDaysCsv: String,
        timesCsv: String
    ) {
        dao.upsertMedication(
            MedicationEntity(
                id = id,
                name = name,
                doseAmount = doseAmount,
                doseUnit = doseUnit,
                notes = notes
            )
        )

        dao.upsertSchedule(
            ScheduleEntity(
                medId = id,
                repeatEveryDay = repeatEveryDay,
                selectedDaysCsv = selectedDaysCsv,
                timesCsv = timesCsv
            )
        )
    }

    override suspend fun deleteMedication(id: String) {
        dao.deleteMedication(id)
    }
}
