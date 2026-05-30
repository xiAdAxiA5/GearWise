package com.example.gearwise.ui.screens.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.gearwise.GearWiseApplication
import com.example.gearwise.data.model.ElectronicItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = (application as GearWiseApplication).repository
    private val itemId: Long = savedStateHandle.get<Long>("itemId") ?: 0L

    val item: StateFlow<ElectronicItem?> = repository.getItemById(itemId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun deleteItem(onDeleted: () -> Unit) {
        viewModelScope.launch {
            item.value?.let { item ->
                repository.delete(item)
                onDeleted()
            }
        }
    }
}
