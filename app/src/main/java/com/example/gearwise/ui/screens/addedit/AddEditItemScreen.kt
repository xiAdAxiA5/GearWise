package com.example.gearwise.ui.screens.addedit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gearwise.util.DateUtils
import java.text.NumberFormat
import java.util.*

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
        topBar = {
            TopAppBar(
                title = {
                    Text(if (viewModel.isEditing) "编辑设备" else "添加设备")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                        Text("保存", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== 基本信息 =====
            SectionHeader("基本信息")

            // 名称
            OutlinedTextField(
                value = formState.name,
                onValueChange = viewModel::updateName,
                label = { Text("名称 *") },
                placeholder = { Text("例如：iPhone 15 Pro") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 分类下拉
            CategoryDropdown(
                selectedCategory = formState.category,
                onCategorySelected = viewModel::updateCategory,
                modifier = Modifier.fillMaxWidth()
            )

            // 品牌
            OutlinedTextField(
                value = formState.brand,
                onValueChange = viewModel::updateBrand,
                label = { Text("品牌") },
                placeholder = { Text("例如：Apple") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 型号
            OutlinedTextField(
                value = formState.model,
                onValueChange = viewModel::updateModel,
                label = { Text("型号") },
                placeholder = { Text("例如：A2848") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ===== 购买信息 =====
            SectionHeader("购买信息")

            // 购买日期
            DatePickerField(
                label = "购买日期 *",
                selectedDateMillis = formState.purchaseDate,
                onDateSelected = viewModel::updatePurchaseDate,
                modifier = Modifier.fillMaxWidth()
            )

            // 购买价格
            OutlinedTextField(
                value = formState.purchasePrice,
                onValueChange = viewModel::updatePurchasePrice,
                label = { Text("购买价格 (¥) *") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("¥ ") },
                modifier = Modifier.fillMaxWidth()
            )

            // 配件支出
            OutlinedTextField(
                value = formState.accessoryCost,
                onValueChange = viewModel::updateAccessoryCost,
                label = { Text("配件支出 (¥)") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("¥ ") },
                supportingText = { Text("保护壳、贴膜、充电器等") },
                modifier = Modifier.fillMaxWidth()
            )

            // 维修支出
            OutlinedTextField(
                value = formState.repairCost,
                onValueChange = viewModel::updateRepairCost,
                label = { Text("维修支出 (¥)") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("¥ ") },
                supportingText = { Text("换屏、换电池、维修费等") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ===== 出售信息 =====
            SectionHeader("出售信息")

            // 是否已出售开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "是否已出售",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = formState.isSold,
                    onCheckedChange = viewModel::updateIsSold
                )
            }

            // 已出售时显示出售日期和出售价格
            if (formState.isSold) {
                DatePickerField(
                    label = "出售日期 *",
                    selectedDateMillis = formState.soldDate,
                    onDateSelected = viewModel::updateSoldDate,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.soldPrice,
                    onValueChange = viewModel::updateSoldPrice,
                    label = { Text("出售价格 (¥)") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("¥ ") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===== 备注 =====
            SectionHeader("备注")

            OutlinedTextField(
                value = formState.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("备注") },
                placeholder = { Text("使用感受、购买原因等...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // 底部留白
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 错误提示
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
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
                    Text("知道了")
                }
            }
        )
    }
}

// ===== 可复用组件 =====

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
    HorizontalDivider()
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
            label = { Text("类型 *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
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
        modifier = modifier,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "选择日期")
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
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
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
