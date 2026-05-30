package com.example.gearwise.ui.screens.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gearwise.data.model.ElectronicItem
import com.example.gearwise.ui.theme.*
import com.example.gearwise.util.DateUtils
import com.example.gearwise.util.DateUtils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    onItemClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    viewModel: ItemListViewModel = viewModel()
) {
    val items by viewModel.items

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GearWise", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加设备")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            // 空状态
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAddClick = onAddClick
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // 顶部统计条
                SummaryBar(
                    totalItems = items.size,
                    activeCount = viewModel.activeCount,
                    grandTotalCost = viewModel.grandTotalCost
                )

                // 设备列表
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            onClick = { onItemClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryBar(
    totalItems: Int,
    activeCount: Int,
    grandTotalCost: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryChip(label = "总计", value = "${totalItems}件")
            SummaryChip(label = "在用", value = "${activeCount}件")
            SummaryChip(label = "总成本", value = formatCurrency(grandTotalCost))
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ItemCard(
    item: ElectronicItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 第一行：图标 + 名称 + 状态
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 分类图标
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = categoryColor(item.category).copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = categoryIcon(item.category),
                            contentDescription = item.category,
                            tint = categoryColor(item.category),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${DateUtils.formatDate(item.purchaseDate)} · ${item.brand}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 状态标签
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (item.isSold) statusSold.copy(alpha = 0.15f) else statusInUse.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (item.isSold) "已出售" else "在用",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.isSold) statusSold else statusInUse,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // 第三行：成本数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CostItem(label = "购买价", value = formatCurrency(item.purchasePrice))
                CostItem(label = "持有天数", value = "${item.daysHeld}天")
                CostItem(label = "实际成本", value = formatCurrency(item.totalCost))
                CostItem(label = "日均成本", value = formatCurrency(item.dailyCost), highlight = true)
            }
        }
    }
}

@Composable
private fun CostItem(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DevicesOther,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "还没有记录任何设备",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击下方按钮添加你的第一台设备",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加设备")
        }
    }
}

/** 分类对应的图标 */
fun categoryIcon(category: String): ImageVector {
    return when (category) {
        "手机" -> Icons.Default.Smartphone
        "电脑" -> Icons.Default.Laptop
        "平板" -> Icons.Default.TabletAndroid
        "耳机" -> Icons.Default.Headphones
        else -> Icons.Default.DevicesOther
    }
}

/** 分类对应的颜色 */
fun categoryColor(category: String): androidx.compose.ui.graphics.Color {
    return when (category) {
        "手机" -> categoryPhone
        "电脑" -> categoryComputer
        "平板" -> categoryTablet
        "耳机" -> categoryHeadphone
        else -> categoryOther
    }
}
