package com.example.gearwise.data.dao

import androidx.room.*
import com.example.gearwise.data.model.CountdownEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface CountdownDao {
    @Query("SELECT * FROM countdown_events ORDER BY targetDate ASC")
    fun getAll(): Flow<List<CountdownEvent>>

    @Query("SELECT * FROM countdown_events WHERE id = :id")
    fun getById(id: Long): Flow<CountdownEvent?>

    @Insert
    suspend fun insert(event: CountdownEvent): Long

    @Update
    suspend fun update(event: CountdownEvent)

    @Delete
    suspend fun delete(event: CountdownEvent)
}
