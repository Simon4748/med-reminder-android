package com.simonatkinson.medicationreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Upsert
import com.simonatkinson.medicationreminder.data.local.entities.MedicationEntity
import com.simonatkinson.medicationreminder.data.local.entities.ScheduleEntity
import kotlinx.coroutines.flow.Flow

data class MedicationWithSchedule(
    @Embedded val medication: MedicationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "medId"
    )
    val schedule: ScheduleEntity
)

@Dao
interface MedicationDao {

    @Upsert
    suspend fun upsertMedication(med: MedicationEntity)

    @Upsert
    suspend fun upsertSchedule(schedule: ScheduleEntity)

    @Transaction
    @Query("SELECT * FROM medications ORDER BY name COLLATE NOCASE ASC")
    fun observeAllWithSchedule(): Flow<List<MedicationWithSchedule>>

    @Query("DELETE FROM medications WHERE id = :medId")
    suspend fun deleteMedication(medId: String)
}
