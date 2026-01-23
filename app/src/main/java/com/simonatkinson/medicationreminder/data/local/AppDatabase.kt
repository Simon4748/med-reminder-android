package com.simonatkinson.medicationreminder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.simonatkinson.medicationreminder.data.local.dao.MedicationDao
import com.simonatkinson.medicationreminder.data.local.entities.MedicationEntity
import com.simonatkinson.medicationreminder.data.local.entities.ScheduleEntity

@Database(
    entities = [
        MedicationEntity::class,
        ScheduleEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "med_reminder.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
