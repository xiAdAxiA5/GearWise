package com.example.gearwise.ui.screens.habits

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.gearwise.data.model.Habit
import com.example.gearwise.data.model.HabitRecord
import com.example.gearwise.data.model.Plan
import com.example.gearwise.ui.components.IconPickerDialog
import com.example.gearwise.ui.components.getIconByName
import com.example.gearwise.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitsViewModel(app: Application) : AndroidViewModel(app) {
    private val db = (app as GearWiseApplication).database
    val habits = db.habitDao().getAllHabits().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val plans = db.planDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleHabit(habitId: Long) = viewModelScope.launch {
        val today = DateUtils.todayTimestamp()
        val r = db.habitDao().getRecord(habitId, today)
        if (r != null) db.habitDao().updateRecord(r.copy(isCompleted = !r.isCompleted, count = if (!r.isCompleted) 1 else 0))
        else db.habitDao().insertRecord(HabitRecord(habitId = habitId, date = today, count = 1, isCompleted = true))
    }
    fun isDone(habitId: Long): StateFlow<Boolean> {
        val today = DateUtils.todayTimestamp(); val r = MutableStateFlow(false)
        viewModelScope.launch { db.habitDao().getRecordsForHabit(habitId).collect { rs -> r.value = rs.any { it.date == today && it.isCompleted } } }; return r
    }
    fun saveHabit(h: Habit, onDone: () -> Unit) = viewModelScope.launch { if (h.id == 0L) db.habitDao().insertHabit(h) else db.habitDao().updateHabit(h); onDone() }
    fun deleteHabit(h: Habit) = viewModelScope.launch { db.habitDao().deleteHabit(h) }
    fun savePlan(p: Plan, onDone: () -> Unit) = viewModelScope.launch { if (p.id == 0L) db.planDao().insert(p) else db.planDao().update(p); onDone() }
    fun deletePlan(p: Plan) = viewModelScope.launch { db.planDao().delete(p) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(viewModel: HabitsViewModel = viewModel()) {
    val habits by viewModel.habits.collectAsState()
    val plans by viewModel.plans.collectAsState()
    var habitEdit by remember { mutableStateOf<Habit?>(null) }
    var planEdit by remember { mutableStateOf<Plan?>(null) }
    var showHabitForm by remember { mutableStateOf(false) }
    var showPlanForm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // === 打卡 ===
        Text("打卡", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (habits.isEmpty()) {
            Card(Modifier.fillMaxWidth().clickable { habitEdit = null; showHabitForm = true }, shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Add, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(8.dp)); Text("添加习惯", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        } else {
            habits.forEach { h ->
                val done by viewModel.isDone(h.id).collectAsState()
                Card(Modifier.fillMaxWidth().clickable { viewModel.toggleHabit(h.id) }, shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline), elevation = CardDefaults.cardElevation(0.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(done, { viewModel.toggleHabit(h.id) }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.onSurface, uncheckedColor = MaterialTheme.colorScheme.outline))
                        Spacer(Modifier.width(6.dp))
                        Icon(getIconByName(h.icon), null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) { Text(h.name, style = MaterialTheme.typography.bodyMedium); Text("${h.frequency} ${h.targetCount}次", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        IconButton(onClick = { habitEdit = h; showHabitForm = true }) { Icon(Icons.Outlined.MoreVert, null, Modifier.size(16.dp)) }
                    }
                }
            }
            TextButton(onClick = { habitEdit = null; showHabitForm = true }) { Icon(Icons.Outlined.Add, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("添加", style = MaterialTheme.typography.bodySmall) }
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

        // === 计划 ===
        Text("计划", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (plans.isEmpty()) {
            Card(Modifier.fillMaxWidth().clickable { planEdit = null; showPlanForm = true }, shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Add, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(8.dp)); Text("添加计划", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        } else {
            plans.sortedByDescending { it.createdAt }.forEach { p ->
                Card(Modifier.fillMaxWidth().clickable { planEdit = p; showPlanForm = true }, shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline), elevation = CardDefaults.cardElevation(0.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(getIconByName(p.icon), null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.title, style = MaterialTheme.typography.bodyMedium)
                            if (p.progress > 0) LinearProgressIndicator(progress = { p.progress / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurface, trackColor = MaterialTheme.colorScheme.outline)
                        }
                        Text(when (p.status) { "completed" -> "✓"; "abandoned" -> "✗"; else -> "${p.progress}%" }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            TextButton(onClick = { planEdit = null; showPlanForm = true }) { Icon(Icons.Outlined.Add, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("添加", style = MaterialTheme.typography.bodySmall) }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showHabitForm) HabitForm(habitEdit, { viewModel.saveHabit(it) { showHabitForm = false } }, { showHabitForm = false }, { if (habitEdit != null) { viewModel.deleteHabit(habitEdit!!); showHabitForm = false } })
    if (showPlanForm) PlanForm(planEdit, { viewModel.savePlan(it) { showPlanForm = false } }, { showPlanForm = false }, { if (planEdit != null) { viewModel.deletePlan(planEdit!!); showPlanForm = false } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitForm(existing: Habit?, onSave: (Habit) -> Unit, onDismiss: () -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: "check_circle") }
    var freq by remember { mutableStateOf(existing?.frequency ?: "daily") }
    var target by remember { mutableStateOf((existing?.targetCount ?: 1).toString()) }
    var iconPick by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(4.dp), containerColor = MaterialTheme.colorScheme.surface, title = { Text(if (existing == null) "添加习惯" else "编辑习惯") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { iconPick = true }) { Icon(getIconByName(icon), null, Modifier.size(24.dp)) }; OutlinedTextField(name, { name = it }, label = { Text("名称") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f)) }
            Row { FilterChip(freq == "daily", { freq = "daily" }, { Text("每天") }, Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); FilterChip(freq == "weekly", { freq = "weekly" }, { Text("每周") }, Modifier.weight(1f)) }
        }
    }, confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onSave(Habit(id = existing?.id ?: 0, name = name, icon = icon, frequency = freq, targetCount = target.toIntOrNull() ?: 1)) }) { Text("保存") } }, dismissButton = { Row { if (existing != null) TextButton(onClick = onDelete) { Text("删除", color = MaterialTheme.colorScheme.error) }; TextButton(onClick = onDismiss) { Text("取消") } } })
    if (iconPick) IconPickerDialog(icon, { icon = it; iconPick = false }, { iconPick = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanForm(existing: Plan?, onSave: (Plan) -> Unit, onDismiss: () -> Unit, onDelete: () -> Unit) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: "flag") }
    var desc by remember { mutableStateOf(existing?.description ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: "short_term") }
    var progress by remember { mutableStateOf((existing?.progress ?: 0).toString()) }
    var status by remember { mutableStateOf(existing?.status ?: "active") }
    var iconPick by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(4.dp), containerColor = MaterialTheme.colorScheme.surface, title = { Text(if (existing == null) "添加计划" else "编辑计划") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { iconPick = true }) { Icon(getIconByName(icon), null, Modifier.size(24.dp)) }; OutlinedTextField(title, { title = it }, label = { Text("名称") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f)) }
            OutlinedTextField(desc, { desc = it }, label = { Text("描述") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth(), minLines = 2)
            Row { FilterChip(type == "short_term", { type = "short_term" }, { Text("短期") }, Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); FilterChip(type == "long_term", { type = "long_term" }, { Text("长期") }, Modifier.weight(1f)) }
            Row { FilterChip(status == "active", { status = "active" }, { Text("进行") }, Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); FilterChip(status == "completed", { status = "completed" }, { Text("完成") }, Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); FilterChip(status == "abandoned", { status = "abandoned" }, { Text("放弃") }, Modifier.weight(1f)) }
        }
    }, confirmButton = { TextButton(onClick = { if (title.isNotBlank()) onSave(Plan(id = existing?.id ?: 0, title = title, icon = icon, description = desc, type = type, status = status, progress = progress.toIntOrNull()?.coerceIn(0, 100) ?: 0)) }) { Text("保存") } }, dismissButton = { Row { if (existing != null) TextButton(onClick = onDelete) { Text("删除", color = MaterialTheme.colorScheme.error) }; TextButton(onClick = onDismiss) { Text("取消") } } })
    if (iconPick) IconPickerDialog(icon, { icon = it; iconPick = false }, { iconPick = false })
}
