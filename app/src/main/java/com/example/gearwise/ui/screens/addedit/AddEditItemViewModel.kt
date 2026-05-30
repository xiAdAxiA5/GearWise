package com.example.gearwise.ui.screens.addedit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.gearwise.GearWiseApplication
import com.example.gearwise.data.model.ElectronicItem
import com.example.gearwise.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormState(
    val name: String = "",
    val category: String = "手机",
    val brand: String = "",
    val model: String = "",
    val purchaseDate: Long = DateUtils.todayTimestamp(),
    val purchasePrice: String = "",
    val accessoryCost: String = "",
    val repairCost: String = "",
    val isSold: Boolean = false,
    val soldDate: Long? = null,
    val soldPrice: String = "",
    val notes: String = ""
) {
    /** 解析购买价格为 Double */
    val purchasePriceValue: Double get() = purchasePrice.toDoubleOrNull() ?: 0.0
    val accessoryCostValue: Double get() = accessoryCost.toDoubleOrNull() ?: 0.0
    val repairCostValue: Double get() = repairCost.toDoubleOrNull() ?: 0.0
    val soldPriceValue: Double? get() = soldPrice.toDoubleOrNull()

    /** 验证必填字段 */
    val isValid: Boolean
        get() {
            if (name.isBlank()) return false
            if (purchasePrice.isBlank() || purchasePriceValue <= 0) return false
            if (isSold && soldDate == null) return false
            if (isSold && soldDate != null && soldDate < purchaseDate) return false
            return true
        }

    /** 转换为 ElectronicItem entity */
    fun toEntity(existingId: Long = 0): ElectronicItem {
        return ElectronicItem(
            id = existingId,
            name = name.trim(),
            category = category,
            brand = brand.trim(),
            model = model.trim(),
            purchaseDate = purchaseDate,
            purchasePrice = purchasePriceValue,
            accessoryCost = accessoryCostValue,
            repairCost = repairCostValue,
            isSold = isSold,
            soldDate = if (isSold) soldDate else null,
            soldPrice = if (isSold) soldPriceValue else null,
            notes = notes.trim()
        )
    }

    companion object {
        fun fromEntity(item: ElectronicItem): FormState {
            return FormState(
                name = item.name,
                category = item.category,
                brand = item.brand,
                model = item.model,
                purchaseDate = item.purchaseDate,
                purchasePrice = if (item.purchasePrice > 0) formatPrice(item.purchasePrice) else "",
                accessoryCost = if (item.accessoryCost > 0) formatPrice(item.accessoryCost) else "",
                repairCost = if (item.repairCost > 0) formatPrice(item.repairCost) else "",
                isSold = item.isSold,
                soldDate = item.soldDate,
                soldPrice = if (item.soldPrice != null && item.soldPrice > 0) formatPrice(item.soldPrice) else "",
                notes = item.notes
            )
        }

        private fun formatPrice(value: Double): String {
            return if (value == value.toLong().toDouble()) {
                value.toLong().toString()
            } else {
                String.format("%.2f", value)
            }
        }
    }
}

class AddEditItemViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = (application as GearWiseApplication).repository
    private val itemId: Long = savedStateHandle.get<Long>("itemId") ?: -1L

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    val isEditing: Boolean get() = itemId != -1L

    init {
        if (isEditing) {
            loadItem()
        }
    }

    private fun loadItem() {
        viewModelScope.launch {
            repository.getItemById(itemId).collect { item ->
                if (item != null) {
                    _formState.value = FormState.fromEntity(item)
                }
            }
        }
    }

    fun updateName(value: String) = _formState.update { it.copy(name = value) }
    fun updateCategory(value: String) = _formState.update { it.copy(category = value) }
    fun updateBrand(value: String) = _formState.update { it.copy(brand = value) }
    fun updateModel(value: String) = _formState.update { it.copy(model = value) }
    fun updatePurchaseDate(value: Long) = _formState.update { it.copy(purchaseDate = value) }
    fun updatePurchasePrice(value: String) = _formState.update { it.copy(purchasePrice = value.filterForPrice()) }
    fun updateAccessoryCost(value: String) = _formState.update { it.copy(accessoryCost = value.filterForPrice()) }
    fun updateRepairCost(value: String) = _formState.update { it.copy(repairCost = value.filterForPrice()) }
    fun updateIsSold(value: Boolean) = _formState.update { it.copy(isSold = value) }
    fun updateSoldDate(value: Long?) = _formState.update { it.copy(soldDate = value) }
    fun updateSoldPrice(value: String) = _formState.update { it.copy(soldPrice = value.filterForPrice()) }
    fun updateNotes(value: String) = _formState.update { it.copy(notes = value) }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val state = _formState.value
            if (!state.isValid) return@launch

            if (isEditing) {
                repository.update(state.toEntity(existingId = itemId))
            } else {
                repository.insert(state.toEntity())
            }
            onSaved()
        }
    }
}

/** 只允许数字和小数点输入 */
private fun String.filterForPrice(): String {
    return this.filter { it.isDigit() || it == '.' }
        .let { s ->
            // 只保留第一个小数点
            val dotIndex = s.indexOf('.')
            if (dotIndex >= 0) {
                s.substring(0, dotIndex + 1) + s.substring(dotIndex + 1).filter { it.isDigit() }
            } else {
                s
            }
        }
        .take(12) // 限制长度
}
