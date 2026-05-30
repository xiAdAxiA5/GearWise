package com.example.gearwise.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gearwise.data.model.ElectronicItem
import com.example.gearwise.ui.screens.list.categoryColor
import com.example.gearwise.ui.screens.list.categoryIcon
import com.example.gearwise.ui.theme.statusInUse
import com.example.gearwise.ui.theme.statusSold
import com.example.gearwise.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Long,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: ItemDetailViewModel = viewModel()
) {
    val item by viewModel.item.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.name ?: "设备详情") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        item?.let { currentItem ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // 头部信息
                DetailHeader(item = currentItem)

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 购买信息
                SectionTitle("购买信息")
                DetailRow("购买日期", DateUtils.formatDate(currentItem.purchaseDate))
                DetailRow("购买价格", DateUtils.formatCurrency(currentItem.purchasePrice))
                DetailRow("配件支出", DateUtils.formatCurrency(currentItem.accessoryCost))
                DetailRow("维修支出", DateUtils.formatCurrency(currentItem.repairCost))

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 处置信息
                SectionTitle("处置信息")
                if (currentItem.isSold) {
                    DetailRow("状态", "已出售")
                    DetailRow(
                        "出售日期",
                        currentItem.soldDate?.let { DateUtils.formatDate(it) } ?: "-"
                    )
                    DetailRow(
                        "出售价格",
                        currentItem.soldPrice?.let { DateUtils.formatCurrency(it) } ?: "-"
                    )
                } else {
                    DetailRow("状态", "仍在用")
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 成本分析
                SectionTitle("成本分析")
                DetailRow("持有天数", "${currentItem.daysHeld} 天")
                DetailRow(
                    "实际持有成本",
                    DateUtils.formatCurrency(currentItem.totalCost),
                    highlight = true
                )
                DetailRow(
                    "日均成本",
                    DateUtils.formatCurrency(currentItem.dailyCost),
                    highlight = true
                )
                DetailRow(
                    "月均成本",
                    DateUtils.formatCurrency(currentItem.monthlyCost)
                )

                // 备注
                if (currentItem.notes.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SectionTitle("备注")
                    Text(
                        text = currentItem.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        } ?: run {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("删除后无法恢复，确定要删除「${item?.name}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteItem(onDeleted)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun DetailHeader(item: ElectronicItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 分类图标
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = categoryColor(item.category).copy(alpha = 0.15f),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = categoryIcon(item.category),
                    contentDescription = item.category,
                    tint = categoryColor(item.category),
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = item.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${item.brand} · ${item.model} · ${item.category}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 状态标签
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (item.isSold) statusSold.copy(alpha = 0.15f)
            else statusInUse.copy(alpha = 0.15f)
        ) {
            Text(
                text = if (item.isSold) "已出售" else "仍在用",
                style = MaterialTheme.typography.labelMedium,
                color = if (item.isSold) statusSold else statusInUse,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}
