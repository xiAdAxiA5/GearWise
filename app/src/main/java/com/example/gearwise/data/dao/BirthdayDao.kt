package com.example.gearwise.data.dao

import androidx.room.*
import com.example.gearwise.data.model.Birthday
import kotlinx.coroutines.flow.Flow

@Dao
interface BirthdayDao {
    @Query("SELECT * FROM birthdays ORDER BY birthDate ASC")
    fun getAll(): Flow<List<Birthday>>

    @Query("SELECT * FROM birthdays WHERE id = :id")
    fun getById(id: Long): Flow<Birthday?>

    @Insert
    suspend fun insert(birthday: Birthday): Long

    @Update
    suspend fun update(birthday: Birthday)

    @Delete
    suspend fun delete(birthday: Birthday)
}
