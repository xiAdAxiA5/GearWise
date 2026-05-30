package com.example.gearwise.ui.screens.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gearwise.GearWiseApplication
import com.example.gearwise.data.model.ElectronicItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ItemListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as GearWiseApplication).repository

    val items: StateFlow<List<ElectronicItem>> = repository.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** 仍在使用的设备数 */
    val activeCount: Int get() = items.value.count { !it.isSold }

    /** 已出售的设备数 */
    val soldCount: Int get() = items.value.count { it.isSold }

    /** 总实际持有成本 */
    val grandTotalCost: Double get() = items.value.sumOf { it.totalCost }
}
