package com.example.gearwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

@Entity(tableName = "countdown_events")
data class CountdownEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "event",        // icon name from preset list
    val targetDate: Long,              // epoch millis
    val type: String = "countdown",    // "countdown" | "countup"
    val isRepeating: Boolean = false,  // yearly repeat
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val daysRemaining: Long
        get() {
            val now = System.currentTimeMillis()
            val days = if (type == "countup") {
                TimeUnit.MILLISECONDS.toDays(now - targetDate)
            } else {
                TimeUnit.MILLISECONDS.toDays(targetDate - now)
            }
            return days.coerceAtLeast(0)
        }

    val label: String
        get() = if (type == "countup") "已经" else "还剩"

    val isOverdue: Boolean
        get() = type == "countdown" && targetDate < System.currentTimeMillis()
}
