package com.example.gearwise.data.dao

import androidx.room.*
import com.example.gearwise.data.model.Habit
import com.example.gearwise.data.model.HabitRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitById(id: Long): Flow<Habit?>

    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // Records
    @Query("SELECT * FROM habit_records WHERE habitId = :habitId ORDER BY date DESC")
    fun getRecordsForHabit(habitId: Long): Flow<List<HabitRecord>>

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getRecord(habitId: Long, date: Long): HabitRecord?

    @Insert
    suspend fun insertRecord(record: HabitRecord)

    @Update
    suspend fun updateRecord(record: HabitRecord)

    @Query("SELECT COUNT(*) FROM habit_records WHERE habitId = :habitId AND isCompleted = 1")
    fun getCompletedCount(habitId: Long): Flow<Int>
}
