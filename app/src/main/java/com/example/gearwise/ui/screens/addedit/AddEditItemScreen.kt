package com.example.gearwise.ui.screens.addedit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gearwise.util.DateUtils

private val categories = listOf("手机", "电脑", "平板", "耳机", "其他")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    itemId: Long?,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddEditItemViewModel = viewModel()
) {
    val formState by viewModel.formState.collectAsState()
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (viewModel.isEditing) "编辑设备" else "添加设备",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (formState.isValid) {
                                viewModel.save(onSaved)
                            } else {
                                showError = true
                            }
                        }
                    ) {
                        Text(
                            "保存",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ===== 基本信息 =====
            SectionLabel("基本信息")

            OutlinedTextField(
                value = formState.name,
                onValueChange = viewModel::updateName,
                label = { Text("名称") },
                placeholder = { Text("例如：iPhone 15 Pro", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = outlinedFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            CategoryDropdown(
                selectedCategory = formState.category,
                onCategorySelected = viewModel::updateCategory,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.brand,
                onValueChange = viewModel::updateBrand,
                label = { Text("品牌") },
                placeholder = { Text("例如：Apple", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = outlinedFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.model,
                onValueChange = viewModel::updateModel,
                label = { Text("型号") },
                placeholder = { Text("例如：A2848", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = outlinedFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ===== 购买信息 =====
            SectionLabel("购买信息")

            DatePickerField(
                label = "购买日期",
                selectedDateMillis = formState.purchaseDate,
                onDateSelected = viewModel::updatePurchaseDate,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.purchasePrice,
                onValueChange = viewModel::updatePurchasePrice,
                label = { Text("购买价格 (¥)") },
                placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("¥ ", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                shape = RoundedCornerShape(4.dp),
                colors = outlinedFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.accessoryCost,
                onValueChange = viewModel::updateAccessoryCost,
                label = { Text("配件支出 (¥)") },
                placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("¥ ", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                supportingText = { Text("保护壳、贴膜、充电器等") },
                shape = RoundedCornerShape(4.dp),
                colors = outlinedFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.repairCost,
                onValueChange = viewModel::updateRepairCost,
                label = { Text("维修支出 (¥)") },
                placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("¥ ", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                supportingText = { Text("换屏、换电池、维修费等") },
                shape = RoundedCornerShape(4.dp),
                colors = outlinedFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ===== 出售信息 =====
            SectionLabel("出售信息")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "是否已出售",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = formState.isSold,
                    onCheckedChange = viewModel::updateIsSold,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.background,
                        checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedThumbColor = MaterialTheme.colorScheme.background,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            if (formState.isSold) {
                DatePickerField(
                    label = "出售日期",
                    selectedDateMillis = formState.soldDate,
                    onDateSelected = viewModel::updateSoldDate,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.soldPrice,
                    onValueChange = viewModel::updateSoldPrice,
                    label = { Text("出售价格 (¥)") },
                    placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("¥ ", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = RoundedCornerShape(4.dp),
                    colors = outlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===== 备注 =====
            SectionLabel("备注")

            OutlinedTextField(
                value = formState.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("备注") },
                placeholder = { Text("使用感受、购买原因等...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(4.dp),
                colors = outlinedFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            shape = RoundedCornerShape(4.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("信息不完整") },
            text = {
                Text(buildString {
                    if (formState.name.isBlank()) appendLine("· 请填写设备名称")
                    if (formState.purchasePrice.isBlank() || formState.purchasePriceValue <= 0) {
                        appendLine("· 请填写有效的购买价格")
                    }
                    if (formState.isSold && formState.soldDate == null) {
                        appendLine("· 请选择出售日期")
                    }
                    if (formState.isSold && formState.soldDate != null &&
                        (formState.soldDate ?: 0L) < formState.purchaseDate
                    ) {
                        appendLine("· 出售日期不能早于购买日期")
                    }
                }.trimEnd())
            },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("知道了", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

// ===== Reusable Components =====

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("类型") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(4.dp),
            colors = outlinedFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(4.dp)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    selectedDateMillis: Long?,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDateMillis?.let { DateUtils.formatDate(it) } ?: "",
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        shape = RoundedCornerShape(4.dp),
        colors = outlinedFieldColors(),
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    Icons.Outlined.DateRange,
                    contentDescription = "选择日期",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = modifier
    )

    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: DateUtils.todayTimestamp()
        )

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    showDialog = false
                }) {
                    Text("确定", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(4.dp)
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.onSurface
)
