package com.example.gearwise.ui.screens.life

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
import com.example.gearwise.data.model.Birthday
import com.example.gearwise.data.model.CountdownEvent
import com.example.gearwise.data.model.DiaryEntry
import com.example.gearwise.ui.components.IconPickerDialog
import com.example.gearwise.ui.components.getIconByName
import com.example.gearwise.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LifeViewModel(app: Application) : AndroidViewModel(app) {
    private val db = (app as GearWiseApplication).database
    val countdowns = db.countdownDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val birthdays = db.birthdayDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val diaries = db.diaryDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveCountdown(e: CountdownEvent, onDone: () -> Unit) = viewModelScope.launch {
        if (e.id == 0L) db.countdownDao().insert(e) else db.countdownDao().update(e); onDone()
    }
    fun saveBirthday(b: Birthday, onDone: () -> Unit) = viewModelScope.launch {
        if (b.id == 0L) db.birthdayDao().insert(b) else db.birthdayDao().update(b); onDone()
    }
    fun saveDiary(d: DiaryEntry, onDone: () -> Unit) = viewModelScope.launch {
        if (d.id == 0L) db.diaryDao().insert(d) else db.diaryDao().update(d.copy(updatedAt = System.currentTimeMillis())); onDone()
    }
    fun deleteCountdown(e: CountdownEvent) = viewModelScope.launch { db.countdownDao().delete(e) }
    fun deleteBirthday(b: Birthday) = viewModelScope.launch { db.birthdayDao().delete(b) }
    fun deleteDiary(d: DiaryEntry) = viewModelScope.launch { db.diaryDao().delete(d) }
}

@Composable
fun LifeScreen(viewModel: LifeViewModel = viewModel()) {
    val countdowns by viewModel.countdowns.collectAsState()
    val birthdays by viewModel.birthdays.collectAsState()
    val diaries by viewModel.diaries.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionBlock("计时", Icons.Outlined.Schedule, countdowns.take(3), { CountdownFormDialog(null, viewModel::saveCountdown, {}) }, null, { c -> "${c.daysRemaining}天 ${c.label}" }, { c -> c.name }) { e -> CountdownFormDialog(e as CountdownEvent, viewModel::saveCountdown, {}) }
        SectionBlock("生日", Icons.Outlined.Cake, birthdays.take(3), { BirthdayFormDialog(null, viewModel::saveBirthday, {}) }, null, { b -> "${b.daysUntilNextBirthday}天 · ${b.age}岁" }, { b -> b.name }) { e -> BirthdayFormDialog(e as Birthday, viewModel::saveBirthday, {}) }
        SectionBlock("日记", Icons.Outlined.EditNote, diaries.take(3), { DiaryFormDialog(null, viewModel::saveDiary, {}) }, { d -> DateUtils.formatDate(d.date) }, { d -> d.content.take(60) }, { d -> d.title.ifBlank { d.content.take(20) } }) { e -> DiaryFormDialog(e as DiaryEntry, viewModel::saveDiary, {}) }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun <T> SectionBlock(
    title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, items: List<T>,
    onAdd: () -> Unit, dateLabel: ((T) -> String)?, subtitle: (T) -> String, titleFn: (T) -> String,
    onItemClick: (T) -> Unit
) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
    if (items.isEmpty()) {
        Card(Modifier.fillMaxWidth().clickable { onAdd() }, shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline), elevation = CardDefaults.cardElevation(0.dp)) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Add, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp)); Text("添加$title", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        items.forEach { item ->
            Card(Modifier.fillMaxWidth().clickable { onItemClick(item) }, shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) { Text(titleFn(item), style = MaterialTheme.typography.bodyMedium, maxLines = 1); Text(subtitle(item), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
                    if (dateLabel != null) Text(dateLabel(item), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onAdd, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.Add, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("添加", style = MaterialTheme.typography.bodySmall) }
            TextButton(onClick = { /* expand to full list */ }, modifier = Modifier.weight(1f)) { Text("查看全部", style = MaterialTheme.typography.bodySmall); Spacer(Modifier.width(4.dp)); Icon(Icons.Outlined.ChevronRight, null, Modifier.size(16.dp)) }
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
    }
}

// === Forms (simplified dialogs) ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountdownFormDialog(existing: CountdownEvent?, onSave: (CountdownEvent) -> Unit, onDismiss: () -> Unit) {
    var show by remember { mutableStateOf(true) }; if (!show) return
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: "event") }
    var target by remember { mutableStateOf(existing?.targetDate ?: DateUtils.todayTimestamp()) }
    var type by remember { mutableStateOf(existing?.type ?: "countdown") }
    var iconPick by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = { show = false }, shape = RoundedCornerShape(4.dp), containerColor = MaterialTheme.colorScheme.surface, title = { Text("计时") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { iconPick = true }) { Icon(getIconByName(icon), null, Modifier.size(24.dp)) }; OutlinedTextField(name, { name = it }, label = { Text("名称") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f)) }
            Row { FilterChip(type == "countdown", { type = "countdown" }, { Text("倒计时") }, Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); FilterChip(type == "countup", { type = "countup" }, { Text("正计时") }, Modifier.weight(1f)) }
            DateField("日期", target) { target = it }
        }
    }, confirmButton = { TextButton(onClick = { if (name.isNotBlank()) { onSave(CountdownEvent(id = existing?.id ?: 0, name = name, icon = icon, targetDate = target, type = type)); show = false } }) { Text("保存") } }, dismissButton = { TextButton(onClick = { show = false }) { Text("取消") } })
    if (iconPick) IconPickerDialog(icon, { icon = it; iconPick = false }, { iconPick = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayFormDialog(existing: Birthday?, onSave: (Birthday) -> Unit, onDismiss: () -> Unit) {
    var show by remember { mutableStateOf(true) }; if (!show) return
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: "cake") }
    var date by remember { mutableStateOf(existing?.birthDate ?: DateUtils.todayTimestamp()) }
    var iconPick by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = { show = false }, shape = RoundedCornerShape(4.dp), containerColor = MaterialTheme.colorScheme.surface, title = { Text("生日") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { iconPick = true }) { Icon(getIconByName(icon), null, Modifier.size(24.dp)) }; OutlinedTextField(name, { name = it }, label = { Text("姓名") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f)) }
            DateField("出生日期", date) { date = it }
        }
    }, confirmButton = { TextButton(onClick = { if (name.isNotBlank()) { onSave(Birthday(id = existing?.id ?: 0, name = name, icon = icon, birthDate = date)); show = false } }) { Text("保存") } }, dismissButton = { TextButton(onClick = { show = false }) { Text("取消") } })
    if (iconPick) IconPickerDialog(icon, { icon = it; iconPick = false }, { iconPick = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryFormDialog(existing: DiaryEntry?, onSave: (DiaryEntry) -> Unit, onDismiss: () -> Unit) {
    var show by remember { mutableStateOf(true) }; if (!show) return
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var content by remember { mutableStateOf(existing?.content ?: "") }
    var mood by remember { mutableStateOf(existing?.mood ?: "") }
    val moods = listOf("😊" to "开心", "😐" to "平淡", "😢" to "难过", "😡" to "生气")
    AlertDialog(onDismissRequest = { show = false }, shape = RoundedCornerShape(4.dp), containerColor = MaterialTheme.colorScheme.surface, title = { Text("日记") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("标题") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { moods.forEach { (emoji, _) -> FilterChip(emoji == mood, { mood = if (mood == emoji) "" else emoji }, { Text(emoji) }) } }
            OutlinedTextField(content, { content = it }, label = { Text("内容") }, minLines = 4, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
        }
    }, confirmButton = { TextButton(onClick = { onSave(DiaryEntry(id = existing?.id ?: 0, date = existing?.date ?: DateUtils.todayTimestamp(), title = title, content = content, mood = mood)); show = false }) { Text("保存") } }, dismissButton = { TextButton(onClick = { show = false }) { Text("取消") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, value: Long, onChange: (Long) -> Unit) {
    var show by remember { mutableStateOf(false) }
    OutlinedTextField(DateUtils.formatDate(value), {}, readOnly = true, label = { Text(label) }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { show = true }) { Icon(Icons.Outlined.DateRange, null) } })
    if (show) DatePickerDialog(onDismissRequest = { show = false }, confirmButton = { TextButton(onClick = { show = false }) { Text("确定") } }) { DatePicker(state = rememberDatePickerState(value)) }
}
