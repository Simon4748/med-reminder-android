package com.simonatkinson.medicationreminder.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medId"], unique = true)]
)
data class ScheduleEntity(
    @PrimaryKey val medId: String,
    val repeatEveryDay: Boolean,
    val selectedDaysCsv: String,
    val timesCsv: String
)
