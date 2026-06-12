package com.example.gearwise.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        /** v1 → v2: 新增 6 个模块的表，保留原有 electronic_items 数据 */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS countdown_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL DEFAULT 'event',
                        targetDate INTEGER NOT NULL,
                        type TEXT NOT NULL DEFAULT 'countdown',
                        isRepeating INTEGER NOT NULL DEFAULT 0,
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS birthdays (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL DEFAULT 'cake',
                        birthDate INTEGER NOT NULL,
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS habits (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL DEFAULT 'check_circle',
                        frequency TEXT NOT NULL DEFAULT 'daily',
                        targetCount INTEGER NOT NULL DEFAULT 1,
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        color TEXT NOT NULL DEFAULT ''
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS habit_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        habitId INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        count INTEGER NOT NULL DEFAULT 0,
                        isCompleted INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS diary_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date INTEGER NOT NULL,
                        title TEXT NOT NULL DEFAULT '',
                        content TEXT NOT NULL DEFAULT '',
                        mood TEXT NOT NULL DEFAULT '',
                        weather TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS subscriptions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL DEFAULT 'subscriptions',
                        appName TEXT NOT NULL DEFAULT '',
                        price REAL NOT NULL DEFAULT 0.0,
                        billingCycle TEXT NOT NULL DEFAULT 'monthly',
                        startDate INTEGER NOT NULL,
                        nextBillingDate INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS plans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        icon TEXT NOT NULL DEFAULT 'flag',
                        description TEXT NOT NULL DEFAULT '',
                        type TEXT NOT NULL DEFAULT 'short_term',
                        status TEXT NOT NULL DEFAULT 'active',
                        startDate INTEGER NOT NULL DEFAULT 0,
                        targetDate INTEGER,
                        progress INTEGER NOT NULL DEFAULT 0,
                        steps TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        @Volatile
        private var INSTANCE: GearWiseDatabase? = null

        fun getDatabase(context: Context): GearWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GearWiseDatabase::class.java,
                    "gearwise_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
