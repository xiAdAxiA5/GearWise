package com.example.gearwise.ui.screens.countdown

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
import com.example.gearwise.data.model.CountdownEvent
import com.example.gearwise.ui.components.IconPickerDialog
import com.example.gearwise.ui.components.getIconByName
import com.example.gearwise.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CountdownViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as GearWiseApplication).database.countdownDao()
    val items: StateFlow<List<CountdownEvent>> = dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun delete(item: CountdownEvent) = viewModelScope.launch { dao.delete(item) }
    fun save(item: CountdownEvent, onDone: () -> Unit) = viewModelScope.launch {
        if (item.id == 0L) dao.insert(item) else dao.update(item)
        onDone()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownScreen(viewModel: CountdownViewModel = viewModel()) {
    val items by viewModel.items.collectAsState()
    var editingItem by remember { mutableStateOf<CountdownEvent?>(null) }
    var showForm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            EmptyMessage("还没有计时事件", "记录值得期待或纪念的日子", onAdd = { editingItem = null; showForm = true })
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items, key = { it.id }) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { editingItem = event; showForm = true },
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(getIconByName(event.icon), null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(event.name, style = MaterialTheme.typography.titleMedium)
                                Text(event.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${event.daysRemaining}天", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(event.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
        FloatingActionButton(
            onClick = { editingItem = null; showForm = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            shape = RoundedCornerShape(4.dp),
            containerColor = MaterialTheme.colorScheme.onBackground,
            contentColor = MaterialTheme.colorScheme.background,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) { Icon(Icons.Outlined.Add, "添加") }
    }

    if (showForm) {
        CountdownFormDialog(event = editingItem, onSave = { viewModel.save(it) { showForm = false } }, onDismiss = { showForm = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountdownFormDialog(event: CountdownEvent?, onSave: (CountdownEvent) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(event?.name ?: "") }
    var icon by remember { mutableStateOf(event?.icon ?: "event") }
    var targetDate by remember { mutableStateOf(event?.targetDate ?: DateUtils.todayTimestamp()) }
    var type by remember { mutableStateOf(event?.type ?: "countdown") }
    var notes by remember { mutableStateOf(event?.notes ?: "") }
    var showIconPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(4.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (event == null) "添加计时" else "编辑计时") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showIconPicker = true }) { Icon(getIconByName(icon), null, Modifier.size(28.dp)) }
                    OutlinedTextField(name, { name = it }, label = { Text("名称") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f))
                }
                Row { FilterChip(type == "countdown", { type = "countdown" }, { Text("倒计时") }, Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); FilterChip(type == "countup", { type = "countup" }, { Text("正计时") }, Modifier.weight(1f)) }
                DateField("目标日期", targetDate) { targetDate = it }
                OutlinedTextField(notes, { notes = it }, label = { Text("备注") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onSave(CountdownEvent(id = event?.id ?: 0, name = name, icon = icon, targetDate = targetDate, type = type, notes = notes))
            }) { Text("保存", color = MaterialTheme.colorScheme.onSurface) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
    if (showIconPicker) IconPickerDialog(icon, { icon = it; showIconPicker = false }, { showIconPicker = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, value: Long, onChange: (Long) -> Unit) {
    var show by remember { mutableStateOf(false) }
    OutlinedTextField(
        DateUtils.formatDate(value), {}, readOnly = true, label = { Text(label) },
        shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth(),
        trailingIcon = { IconButton(onClick = { show = true }) { Icon(Icons.Outlined.DateRange, null) } }
    )
    if (show) {
        DatePickerDialog(onDismissRequest = { show = false }, confirmButton = { TextButton(onClick = { show = false }) { Text("确定") } }) {
            DatePicker(state = rememberDatePickerState(value))
        }
    }
}

@Composable
private fun EmptyMessage(title: String, subtitle: String, onAdd: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Schedule, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}
