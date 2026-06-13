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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LifeViewModel(app: Application) : AndroidViewModel(app) {
    private val db = (app as GearWiseApplication).database
    val countdowns = db.countdownDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val birthdays = db.birthdayDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val diaries = db.diaryDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveCountdown(e: CountdownEvent, onDone: () -> Unit) = viewModelScope.launch { if (e.id == 0L) db.countdownDao().insert(e) else db.countdownDao().update(e); onDone() }
    fun saveBirthday(b: Birthday, onDone: () -> Unit) = viewModelScope.launch { if (b.id == 0L) db.birthdayDao().insert(b) else db.birthdayDao().update(b); onDone() }
    fun saveDiary(d: DiaryEntry, onDone: () -> Unit) = viewModelScope.launch { if (d.id == 0L) db.diaryDao().insert(d) else db.diaryDao().update(d.copy(updatedAt = System.currentTimeMillis())); onDone() }
}

@Composable
fun LifeScreen(viewModel: LifeViewModel = viewModel()) {
    val countdowns by viewModel.countdowns.collectAsState()
    val birthdays by viewModel.birthdays.collectAsState()
    val diaries by viewModel.diaries.collectAsState()

    // Form state
    var editingCountdown by remember { mutableStateOf<CountdownEvent?>(null) }; var showCountdownForm by remember { mutableStateOf(false) }
    var editingBirthday by remember { mutableStateOf<Birthday?>(null) }; var showBirthdayForm by remember { mutableStateOf(false) }
    var editingDiary by remember { mutableStateOf<DiaryEntry?>(null) }; var showDiaryForm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // === 计时 ===
        SectionLabel("计时", Icons.Outlined.Schedule)
        countdowns.take(3).forEach { c ->
            Card(Modifier.fillMaxWidth().clickable { editingCountdown = c; showCountdownForm = true }, shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(getIconByName(c.icon), null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(c.name, style = MaterialTheme.typography.bodyMedium); Text(c.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }; Text("${c.daysRemaining}天 ${c.label}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        if (countdowns.isEmpty()) EmptyHint("添加计时") { editingCountdown = null; showCountdownForm = true }
        else TextButton(onClick = { editingCountdown = null; showCountdownForm = true }) { Icon(Icons.Outlined.Add, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("添加计时", style = MaterialTheme.typography.bodySmall) }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

        // === 生日 ===
        SectionLabel("生日", Icons.Outlined.Cake)
        birthdays.take(3).forEach { b ->
            Card(Modifier.fillMaxWidth().clickable { editingBirthday = b; showBirthdayForm = true }, shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(getIconByName(b.icon), null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(b.name, style = MaterialTheme.typography.bodyMedium); Text("${b.age}岁 · ${b.zodiac} · ${b.constellation}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text("${b.daysUntilNextBirthday}天", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        if (birthdays.isEmpty()) EmptyHint("添加生日") { editingBirthday = null; showBirthdayForm = true }
        else TextButton(onClick = { editingBirthday = null; showBirthdayForm = true }) { Icon(Icons.Outlined.Add, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("添加生日", style = MaterialTheme.typography.bodySmall) }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

        // === 日记 ===
        SectionLabel("日记", Icons.Outlined.EditNote)
        diaries.take(3).forEach { d ->
            Card(Modifier.fillMaxWidth().clickable { editingDiary = d; showDiaryForm = true }, shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(d.title.ifBlank { d.content.take(30) }, style = MaterialTheme.typography.bodyMedium, maxLines = 1); Text(DateUtils.formatDate(d.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; if (d.mood.isNotBlank()) Text(d.mood, style = MaterialTheme.typography.labelMedium) }
            }
        }
        if (diaries.isEmpty()) EmptyHint("写日记") { editingDiary = null; showDiaryForm = true }
        else TextButton(onClick = { editingDiary = null; showDiaryForm = true }) { Icon(Icons.Outlined.Add, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("写日记", style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(16.dp))
    }

    // Forms
    if (showCountdownForm) CountdownFormDialog(editingCountdown, { viewModel.saveCountdown(it) { showCountdownForm = false } }, { showCountdownForm = false })
    if (showBirthdayForm) BirthdayFormDialog(editingBirthday, { viewModel.saveBirthday(it) { showBirthdayForm = false } }, { showBirthdayForm = false })
    if (showDiaryForm) DiaryFormDialog(editingDiary, { viewModel.saveDiary(it) { showDiaryForm = false } }, { showDiaryForm = false })
}

@Composable
private fun SectionLabel(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(6.dp)); Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
private fun EmptyHint(label: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Add, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(8.dp)); Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

// === Countdown Form ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountdownFormDialog(existing: CountdownEvent?, onSave: (CountdownEvent) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: "event") }
    var target by remember { mutableStateOf(existing?.targetDate ?: DateUtils.todayTimestamp()) }
    var type by remember { mutableStateOf(existing?.type ?: "countdown") }
    var iconPick by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(4.dp), containerColor = MaterialTheme.colorScheme.surface, title = { Text(if (existing == null) "添加计时" else "编辑计时") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { iconPick = true }) { Icon(getIconByName(icon), null, Modifier.size(24.dp)) }; OutlinedTextField(name, { name = it }, label = { Text("名称") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f)) }
            Row { FilterChip(type == "countdown", { type = "countdown" }, { Text("倒计时") }, Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); FilterChip(type == "countup", { type = "countup" }, { Text("正计时") }, Modifier.weight(1f)) }
            DateField("日期", target) { target = it }
        }
    }, confirmButton = { TextButton(onClick = { if (name.isNotBlank()) { onSave(CountdownEvent(id = existing?.id ?: 0, name = name, icon = icon, targetDate = target, type = type)) } }) { Text("保存") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
    if (iconPick) IconPickerDialog(icon, { icon = it; iconPick = false }, { iconPick = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayFormDialog(existing: Birthday?, onSave: (Birthday) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: "cake") }
    var date by remember { mutableStateOf(existing?.birthDate ?: DateUtils.todayTimestamp()) }
    var iconPick by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(4.dp), containerColor = MaterialTheme.colorScheme.surface, title = { Text(if (existing == null) "添加生日" else "编辑生日") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { iconPick = true }) { Icon(getIconByName(icon), null, Modifier.size(24.dp)) }; OutlinedTextField(name, { name = it }, label = { Text("姓名") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f)) }
            DateField("出生日期", date) { date = it }
        }
    }, confirmButton = { TextButton(onClick = { if (name.isNotBlank()) { onSave(Birthday(id = existing?.id ?: 0, name = name, icon = icon, birthDate = date)) } }) { Text("保存") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
    if (iconPick) IconPickerDialog(icon, { icon = it; iconPick = false }, { iconPick = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryFormDialog(existing: DiaryEntry?, onSave: (DiaryEntry) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var content by remember { mutableStateOf(existing?.content ?: "") }
    var mood by remember { mutableStateOf(existing?.mood ?: "") }
    val moods = listOf("😊" to "开心", "😐" to "平淡", "😢" to "难过", "😡" to "生气")
    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(4.dp), containerColor = MaterialTheme.colorScheme.surface, title = { Text(if (existing == null) "写日记" else DateUtils.formatDate(existing!!.date)) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("标题") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { moods.forEach { (emoji, _) -> FilterChip(emoji == mood, { mood = if (mood == emoji) "" else emoji }, { Text(emoji) }) } }
            OutlinedTextField(content, { content = it }, label = { Text("内容") }, minLines = 4, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
        }
    }, confirmButton = { TextButton(onClick = { onSave(DiaryEntry(id = existing?.id ?: 0, date = existing?.date ?: DateUtils.todayTimestamp(), title = title, content = content, mood = mood)) }) { Text("保存") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, value: Long, onChange: (Long) -> Unit) {
    var show by remember { mutableStateOf(false) }
    OutlinedTextField(DateUtils.formatDate(value), {}, readOnly = true, label = { Text(label) }, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { show = true }) { Icon(Icons.Outlined.DateRange, null) } })
    if (show) DatePickerDialog(onDismissRequest = { show = false }, confirmButton = { TextButton(onClick = { show = false }) { Text("确定") } }) { DatePicker(state = rememberDatePickerState(value)) }
}
