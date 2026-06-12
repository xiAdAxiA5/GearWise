package com.example.gearwise.ui.screens.plans

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
import com.example.gearwise.data.model.Plan
import com.example.gearwise.ui.components.IconPickerDialog
import com.example.gearwise.ui.components.getIconByName
import com.example.gearwise.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlanViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as GearWiseApplication).database.planDao()
    val items: StateFlow<List<Plan>> = dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun save(plan: Plan, onDone: () -> Unit) = viewModelScope.launch {
        if (plan.id == 0L) dao.insert(plan) else dao.update(plan)
        onDone()
    }
    fun delete(plan: Plan) = viewModelScope.launch { dao.delete(plan) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(viewModel: PlanViewModel = viewModel()) {
    val items by viewModel.items.collectAsState()
    var editing by remember { mutableStateOf<Plan?>(null) }
    var showForm by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (items.isEmpty()) EmptyMsg("还没有计划", onAdd = { editing = null; showForm = true })
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.id }) { plan ->
                Card(
                    Modifier.fillMaxWidth().clickable { editing = plan; showForm = true },
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(getIconByName(plan.icon), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(plan.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.width(8.dp))
                                Text(if (plan.type == "short_term") "短期" else "长期", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (plan.description.isNotBlank()) Text(plan.description.take(60), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            if (plan.progress > 0) LinearProgressIndicator(progress = { plan.progress / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp), color = MaterialTheme.colorScheme.onSurface, trackColor = MaterialTheme.colorScheme.outline)
                        }
                        Text(
                            when (plan.status) { "completed" -> "✓ 完成"; "abandoned" -> "已放弃"; else -> if (plan.isOverdue) "已超期" else plan.daysRemaining?.let { "${it}天" } ?: "-" },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

    if (showForm) PlanForm(editing, { viewModel.save(it) { showForm = false } }, { showForm = false }, { if (editing != null) { viewModel.delete(editing!!); showForm = false } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanForm(existing: Plan?, onSave: (Plan) -> Unit, onDismiss: () -> Unit, onDelete: () -> Unit) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: "flag") }
    var desc by remember { mutableStateOf(existing?.description ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: "short_term") }
    var status by remember { mutableStateOf(existing?.status ?: "active") }
    var progress by remember { mutableStateOf((existing?.progress ?: 0).toString()) }
    var showIconPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss, shape = RoundedCornerShape(4.dp), containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (existing == null) "添加计划" else "编辑计划") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showIconPicker = true }) { Icon(getIconByName(icon), null, Modifier.size(28.dp)) }
                    OutlinedTextField(title, { title = it }, label = { Text("计划名称") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f))
                }
                OutlinedTextField(desc, { desc = it }, label = { Text("描述") }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth(), minLines = 2)
                Row {
                    FilterChip(type == "short_term", { type = "short_term" }, { Text("短期") }, Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    FilterChip(type == "long_term", { type = "long_term" }, { Text("长期") }, Modifier.weight(1f))
                }
                OutlinedTextField(progress, { progress = it.filter { it.isDigit() }.take(3) }, label = { Text("进度 (0-100)") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
                Row {
                    FilterChip(status == "active", { status = "active" }, { Text("进行中") }, Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    FilterChip(status == "completed", { status = "completed" }, { Text("已完成") }, Modifier.weight(1f))
                }
            }
        },
        confirmButton = { TextButton(onClick = { if (title.isNotBlank()) onSave(Plan(id = existing?.id ?: 0, title = title, icon = icon, description = desc, type = type, status = status, progress = progress.toIntOrNull()?.coerceIn(0, 100) ?: 0)) }) { Text("保存", color = MaterialTheme.colorScheme.onSurface) } },
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
        Icon(Icons.Outlined.Flag, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
