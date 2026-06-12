package com.example.gearwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,                    // epoch millis (start of day) — one entry per day
    val title: String = "",
    val content: String = "",
    val mood: String = "",             // optional emoji or label
    val weather: String = "",          // optional
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
