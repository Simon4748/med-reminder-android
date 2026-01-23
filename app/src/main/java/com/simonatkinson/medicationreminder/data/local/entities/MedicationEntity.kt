package com.simonatkinson.medicationreminder.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val doseAmount: String,
    val doseUnit: String,
    val notes: String
)
