package com.example.gearwise.data.repository

import com.example.gearwise.data.dao.ElectronicItemDao
import com.example.gearwise.data.model.ElectronicItem
import kotlinx.coroutines.flow.Flow

class ItemRepository(private val dao: ElectronicItemDao) {

    fun getAllItems(): Flow<List<ElectronicItem>> = dao.getAllItems()

    fun getItemById(id: Long): Flow<ElectronicItem?> = dao.getItemById(id)

    suspend fun insert(item: ElectronicItem): Long = dao.insertItem(item)

    suspend fun update(item: ElectronicItem) = dao.updateItem(item)

    suspend fun delete(item: ElectronicItem) = dao.deleteItem(item)
}
