package com.example.gearwise.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gearwise.data.dao.*
import com.example.gearwise.data.model.*

@Database(
    entities = [
        ElectronicItem::class,
        CountdownEvent::class,
        Birthday::class,
        Habit::class,
        HabitRecord::class,
        DiaryEntry::class,
        Subscription::class,
        Plan::class
    ],
    version = 2,
    exportSchema = false
)
abstract class GearWiseDatabase : RoomDatabase() {

    abstract fun electronicItemDao(): ElectronicItemDao
    abstract fun countdownDao(): CountdownDao
    abstract fun birthdayDao(): BirthdayDao
    abstract fun habitDao(): HabitDao
    abstract fun diaryDao(): DiaryDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun planDao(): PlanDao

    companion object {
        @Volatile
        private var INSTANCE: GearWiseDatabase? = null

        fun getDatabase(context: Context): GearWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GearWiseDatabase::class.java,
                    "gearwise_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
