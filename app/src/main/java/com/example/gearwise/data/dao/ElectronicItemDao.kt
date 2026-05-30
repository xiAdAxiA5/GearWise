package com.example.gearwise.data.dao

import androidx.room.*
import com.example.gearwise.data.model.ElectronicItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ElectronicItemDao {

    /** 按购买日期倒序获取全部设备 */
    @Query("SELECT * FROM electronic_items ORDER BY purchaseDate DESC")
    fun getAllItems(): Flow<List<ElectronicItem>>

    /** 根据 ID 获取单个设备 */
    @Query("SELECT * FROM electronic_items WHERE id = :id")
    fun getItemById(id: Long): Flow<ElectronicItem?>

    /** 插入新设备，返回自增 ID */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ElectronicItem): Long

    /** 更新已有设备 */
    @Update
    suspend fun updateItem(item: ElectronicItem)

    /** 删除设备 */
    @Delete
    suspend fun deleteItem(item: ElectronicItem)
}
