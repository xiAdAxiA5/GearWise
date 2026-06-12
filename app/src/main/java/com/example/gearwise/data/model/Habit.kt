package com.example.gearwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "check_circle",
    val frequency: String = "daily",   // "daily" | "weekly" | "custom"
    val targetCount: Int = 1,          // target per period
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val color: String = ""             // optional tint (unused in mono theme, kept for future)
)

@Entity(tableName = "habit_records")
data class HabitRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: Long,                    // epoch millis (start of day)
    val count: Int = 0,                // actual count completed
    val isCompleted: Boolean = false
)
