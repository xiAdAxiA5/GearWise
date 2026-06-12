package com.example.gearwise.ui.screens.habits

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gearwise.GearWiseApplication
import com.example.gearwise.data.model.Habit
import com.example.gearwise.data.model.HabitRecord
import com.example.gearwise.ui.components.IconPickerDialog
import com.example.gearwise.ui.components.getIconByName
import com.example.gearwise.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class HabitViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as GearWiseApplication).database.habitDao()
    val habits: StateFlow<List<Habit>> = dao.getAllHabits().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleToday(habitId: Long) = viewModelScope.launch {
        val today = DateUtils.todayTimestamp()
        val existing = dao.getRecord(habitId, today)
        if (existing != null) {
            dao.updateRecord(existing.copy(isCompleted = !existing.isCompleted, count = if (!existing.isCompleted) 1 else 0))
        } else {
            dao.insertRecord(HabitRecord(habitId = habitId, date = today, count = 1, isCompleted = true))
        }
    }

    fun isCompletedToday(habitId: Long): StateFlow<Boolean> {
        val today = DateUtils.todayTimestamp()
        val result = MutableStateFlow(false)
        viewModelScope.launch {
            dao.getRecordsForHabit(habitId).collect { records ->
                result.value = records.any { it.date == today && it.isCompleted }
            }
        }
        return result
    }

    fun save(habit: Habit, onDone: () -> Unit) = viewModelScope.launch {
        if (habit.id == 0L) dao.insertHabit(habit) else dao.updateHabit(habit)
        onDone()
    }

    fun delete(habit: Habit) = viewModelScope.launch { dao.deleteHabit(habit) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(viewModel: HabitViewModel = viewModel()) {
    val habits by viewModel.habits.collectAsState()
    var editing by remember { mutableStateOf<Habit?>(null) }
    var showForm by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (habits.isEmpty()) EmptyMsg("还没有打卡习惯", onAdd = { editing = null; showForm = true })
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(habits, key = { it.id }) { habit ->
                val isDone by viewModel.isCompletedToday(habit.id).collectAsState()
                Card(
                    Modifier.fillMaxWidth().clickable { viewModel.toggleToday(habit.id) },
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { viewModel.toggleToday(habit.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.onSurface,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(getIconByName(habit.icon), null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(habit.name, style = MaterialTheme.typography.titleMedium)
                            Text("${habit.frequency} · 目标${habit.targetCount}次", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { editing = habit; showForm = true }) { Icon(Icons.Outlined.MoreVert, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
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

    if (showForm) HabitForm(editing, { viewModel.save(it) { showForm = false } }, { showForm = false }, { if (editing != null) { viewModel.delete(editing!!); showForm = false } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitForm(existing: Habit?, onSave: (Habit) -> Unit, onDismiss: () -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: "check_circle") }
    var freq by remember { mutableStateOf(existing?.frequency ?: "daily") }
    var target by remember { mutableStateOf((existing?.targetCount ?: 1).toString()) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var showIconPicker by remember { mutableStateOf(false) }
    val freqs = listOf("daily" to "每天", "weekly" to "每周")

    AlertDialog(
        onDismissRequest = onDismiss, shape = RoundedCornerShape(4.dp), containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (existing == null) "添加习惯" else "编辑习惯") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showIconPicker = true }) { Icon(getIconByName(icon), null, Modifier.size(28.dp)) }
                    OutlinedTextField(name, { name = it }, label = { Text("习惯名称") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f))
                }
                Row { freqs.forEach { (k, v) -> FilterChip(k == freq, { freq = k }, { Text(v) }, Modifier.weight(1f)); Spacer(Modifier.width(8.dp)) } }
                OutlinedTextField(target, { target = it.filter { it.isDigit() } }, label = { Text("每日目标次数") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onSave(Habit(id = existing?.id ?: 0, name = name, icon = icon, frequency = freq, targetCount = target.toIntOrNull() ?: 1, notes = notes)) }) { Text("保存", color = MaterialTheme.colorScheme.onSurface) } },
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
        Icon(Icons.Outlined.CheckCircle, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
