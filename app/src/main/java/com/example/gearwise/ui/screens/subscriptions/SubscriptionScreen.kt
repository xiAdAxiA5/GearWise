package com.example.gearwise.ui.screens.subscriptions

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gearwise.GearWiseApplication
import com.example.gearwise.data.model.Subscription
import com.example.gearwise.ui.components.IconPickerDialog
import com.example.gearwise.ui.components.getIconByName
import com.example.gearwise.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubscriptionViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as GearWiseApplication).database.subscriptionDao()
    val items: StateFlow<List<Subscription>> = dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun save(item: Subscription, onDone: () -> Unit) = viewModelScope.launch {
        if (item.id == 0L) dao.insert(item) else dao.update(item)
        onDone()
    }
    fun delete(item: Subscription) = viewModelScope.launch { dao.delete(item) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(viewModel: SubscriptionViewModel = viewModel()) {
    val items by viewModel.items.collectAsState()
    var editing by remember { mutableStateOf<Subscription?>(null) }
    var showForm by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (items.isEmpty()) EmptyMsg("还没有订阅记录", onAdd = { editing = null; showForm = true })
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                val total = items.sumOf { it.monthlyCost }
                Text("月均总支出 ${DateUtils.formatCurrency(total)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
            }
            items(items, key = { it.id }) { sub ->
                Card(
                    Modifier.fillMaxWidth().clickable { editing = sub; showForm = true },
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(getIconByName(sub.icon), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(sub.name, style = MaterialTheme.typography.titleMedium)
                                if (!sub.isActive) { Spacer(Modifier.width(8.dp)); Text("已停用", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                            Text("${DateUtils.formatCurrency(sub.price)}/${cycleLabel(sub.billingCycle)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(DateUtils.formatCurrency(sub.monthlyCost), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("月均", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
        FloatingActionButton(
            onClick = { editing = null; showForm = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            shape = RoundedCornerShape(4.dp),
            containerColor = MaterialTheme.colorScheme.onBackground,
            contentColor = MaterialTheme.colorScheme.background,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) { Icon(Icons.Outlined.Add, "添加") }
    }

    if (showForm) SubForm(editing, { viewModel.save(it) { showForm = false } }, { showForm = false }, { if (editing != null) { viewModel.delete(editing!!); showForm = false } })
}

private fun cycleLabel(c: String) = when (c) { "weekly" -> "周"; "yearly" -> "年"; else -> "月" }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubForm(existing: Subscription?, onSave: (Subscription) -> Unit, onDismiss: () -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: "subscriptions") }
    var appName by remember { mutableStateOf(existing?.appName ?: "") }
    var price by remember { mutableStateOf(if (existing?.price ?: 0.0 > 0) existing!!.price.toInt().toString() else "") }
    var cycle by remember { mutableStateOf(existing?.billingCycle ?: "monthly") }
    var startDate by remember { mutableStateOf(existing?.startDate ?: DateUtils.todayTimestamp()) }
    var isActive by remember { mutableStateOf(existing?.isActive ?: true) }
    var showIconPicker by remember { mutableStateOf(false) }
    val cycles = listOf("monthly" to "月付", "yearly" to "年付", "weekly" to "周付")

    AlertDialog(
        onDismissRequest = onDismiss, shape = RoundedCornerShape(4.dp), containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (existing == null) "添加订阅" else "编辑订阅") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showIconPicker = true }) { Icon(getIconByName(icon), null, Modifier.size(28.dp)) }
                    OutlinedTextField(name, { name = it }, label = { Text("服务名称") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f))
                }
                OutlinedTextField(appName, { appName = it }, label = { Text("App/平台") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(price, { price = it.filter { it.isDigit() || it == '.' } }, label = { Text("价格 (¥)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), prefix = { Text("¥ ") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
                Row { cycles.forEach { (k, v) -> FilterChip(k == cycle, { cycle = k }, { Text(v) }, Modifier.weight(1f)); Spacer(Modifier.width(8.dp)) } }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text("是否在用"); Switch(isActive, { isActive = it }, colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.onSurface, checkedThumbColor = MaterialTheme.colorScheme.background)) }
            }
        },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onSave(Subscription(id = existing?.id ?: 0, name = name, icon = icon, appName = appName, price = price.toDoubleOrNull() ?: 0.0, billingCycle = cycle, startDate = startDate, nextBillingDate = startDate, isActive = isActive)) }) { Text("保存", color = MaterialTheme.colorScheme.onSurface) } },
        dismissButton = {
            Row {
                if (existing != null) TextButton(onClick = onDelete) { Text("删除", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    )
    if (showIconPicker) IconPickerDialog(icon, { icon = it; showIconPicker = false }, { showIconPicker = false })
}

@Composable
private fun EmptyMsg(title: String, onAdd: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Subscriptions, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
