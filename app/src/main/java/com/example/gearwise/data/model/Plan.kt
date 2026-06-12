package com.example.gearwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

@Entity(tableName = "plans")
data class Plan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val icon: String = "flag",
    val description: String = "",
    val type: String = "short_term",   // "short_term" | "long_term"
    val status: String = "active",     // "active" | "completed" | "abandoned"
    val startDate: Long = System.currentTimeMillis(),
    val targetDate: Long? = null,      // optional deadline
    val progress: Int = 0,             // 0-100
    val steps: String = "",            // JSON array of milestone strings, simple for now
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val daysRemaining: Long?
        get() {
            if (targetDate == null) return null
            val days = TimeUnit.MILLISECONDS.toDays(targetDate - System.currentTimeMillis())
            return days.coerceAtLeast(0)
        }

    val isOverdue: Boolean
        get() = targetDate != null && targetDate < System.currentTimeMillis() && status == "active"
}
