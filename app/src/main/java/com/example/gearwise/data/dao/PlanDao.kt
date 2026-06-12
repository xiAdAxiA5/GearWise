package com.example.gearwise.data.dao

import androidx.room.*
import com.example.gearwise.data.model.Plan
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Plan>>

    @Query("SELECT * FROM plans WHERE id = :id")
    fun getById(id: Long): Flow<Plan?>

    @Insert
    suspend fun insert(plan: Plan): Long

    @Update
    suspend fun update(plan: Plan)

    @Delete
    suspend fun delete(plan: Plan)
}
