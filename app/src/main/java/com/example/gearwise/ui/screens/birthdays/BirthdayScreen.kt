package com.example.gearwise.ui.screens.birthdays

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gearwise.GearWiseApplication
import com.example.gearwise.data.model.Birthday
import com.example.gearwise.ui.components.IconPickerDialog
import com.example.gearwise.ui.components.getIconByName
import com.example.gearwise.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BirthdayViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as GearWiseApplication).database.birthdayDao()
    val items: StateFlow<List<Birthday>> = dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun delete(item: Birthday) = viewModelScope.launch { dao.delete(item) }
    fun save(item: Birthday, onDone: () -> Unit) = viewModelScope.launch {
        if (item.id == 0L) dao.insert(item) else dao.update(item)
        onDone()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayScreen(viewModel: BirthdayViewModel = viewModel()) {
    val items by viewModel.items.collectAsState()
    var editing by remember { mutableStateOf<Birthday?>(null) }
    var showForm by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (items.isEmpty()) EmptyMessage("还没有生日记录", onAdd = { editing = null; showForm = true })
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.id }) { b ->
                Card(
                    Modifier.fillMaxWidth().clickable { editing = b; showForm = true },
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(getIconByName(b.icon), null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(b.name, style = MaterialTheme.typography.titleMedium)
                            Text("${b.age}岁 · ${b.zodiac} · ${b.constellation}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${b.daysUntilNextBirthday}天", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("距生日", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    if (showForm) BirthdayForm(editing, { viewModel.save(it) { showForm = false } }, { showForm = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayForm(existing: Birthday?, onSave: (Birthday) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: "cake") }
    var birthDate by remember { mutableStateOf(existing?.birthDate ?: DateUtils.todayTimestamp()) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var showIconPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss, shape = RoundedCornerShape(4.dp), containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (existing == null) "添加生日" else "编辑生日") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showIconPicker = true }) { Icon(getIconByName(icon), null, Modifier.size(28.dp)) }
                    OutlinedTextField(name, { name = it }, label = { Text("姓名") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f))
                }
                DateField("出生日期", birthDate) { birthDate = it }
                OutlinedTextField(notes, { notes = it }, label = { Text("备注") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onSave(Birthday(id = existing?.id ?: 0, name = name, icon = icon, birthDate = birthDate, notes = notes)) }) { Text("保存", color = MaterialTheme.colorScheme.onSurface) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
    if (showIconPicker) IconPickerDialog(icon, { icon = it; showIconPicker = false }, { showIconPicker = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, value: Long, onChange: (Long) -> Unit) {
    var show by remember { mutableStateOf(false) }
    OutlinedTextField(DateUtils.formatDate(value), {}, readOnly = true, label = { Text(label) }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { show = true }) { Icon(Icons.Outlined.DateRange, null) } })
    if (show) DatePickerDialog(onDismissRequest = { show = false }, confirmButton = { TextButton(onClick = {
        show = false
        // DatePicker doesn't expose state directly — use timestamp from the state
    }) { Text("确定") } }) {
        val s = rememberDatePickerState(value)
        DatePicker(state = s)
        LaunchedEffect(s.selectedDateMillis) { s.selectedDateMillis?.let { onChange(it) } }
    }
}

@Composable
private fun EmptyMessage(title: String, onAdd: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Cake, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
