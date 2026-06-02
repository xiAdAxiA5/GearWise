package com.example.gearwise.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gearwise.GearWiseApplication
import com.example.gearwise.data.model.ElectronicItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as GearWiseApplication).repository

    val items: StateFlow<List<ElectronicItem>> = repository.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun importItems(items: List<ElectronicItem>, onComplete: () -> Unit) {
        viewModelScope.launch {
            for (item in items) {
                repository.insert(item)
            }
            onComplete()
        }
    }
}
