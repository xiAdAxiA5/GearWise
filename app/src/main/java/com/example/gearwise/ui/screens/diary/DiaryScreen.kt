package com.example.gearwise.ui.screens.diary

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
import com.example.gearwise.data.model.DiaryEntry
import com.example.gearwise.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiaryViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as GearWiseApplication).database.diaryDao()
    val entries: StateFlow<List<DiaryEntry>> = dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun save(entry: DiaryEntry, onDone: () -> Unit) = viewModelScope.launch {
        if (entry.id == 0L) dao.insert(entry) else dao.update(entry.copy(updatedAt = System.currentTimeMillis()))
        onDone()
    }
    fun delete(entry: DiaryEntry) = viewModelScope.launch { dao.delete(entry) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(viewModel: DiaryViewModel = viewModel()) {
    val entries by viewModel.entries.collectAsState()
    var editing by remember { mutableStateOf<DiaryEntry?>(null) }
    var showForm by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (entries.isEmpty()) EmptyMsg("还没有日记", onAdd = { editing = null; showForm = true })
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(entries, key = { it.id }) { entry ->
                Card(
                    Modifier.fillMaxWidth().clickable { editing = entry; showForm = true },
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(DateUtils.formatDate(entry.date), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (entry.mood.isNotBlank()) { Spacer(Modifier.width(8.dp)); Text(entry.mood, style = MaterialTheme.typography.bodySmall) }
                            if (entry.weather.isNotBlank()) { Spacer(Modifier.width(8.dp)); Text(entry.weather, style = MaterialTheme.typography.bodySmall) }
                        }
                        if (entry.title.isNotBlank()) { Spacer(Modifier.height(6.dp)); Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium) }
                        if (entry.content.isNotBlank()) { Spacer(Modifier.height(6.dp)); Text(entry.content.take(120), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3) }
                    }
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
        FloatingActionButton(
            onClick = {
                val today = DateUtils.todayTimestamp()
                viewModel.entries.value.find { it.date == today }?.let { editing = it } ?: run { editing = null }
                showForm = true
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            shape = RoundedCornerShape(4.dp),
            containerColor = MaterialTheme.colorScheme.onBackground,
            contentColor = MaterialTheme.colorScheme.background,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) { Icon(Icons.Outlined.Edit, "写日记") }
    }

    if (showForm) DiaryForm(editing, { viewModel.save(it) { showForm = false } }, { showForm = false }, { if (editing != null) { viewModel.delete(editing!!); showForm = false } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryForm(existing: DiaryEntry?, onSave: (DiaryEntry) -> Unit, onDismiss: () -> Unit, onDelete: () -> Unit) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var content by remember { mutableStateOf(existing?.content ?: "") }
    var mood by remember { mutableStateOf(existing?.mood ?: "") }
    val moods = listOf("😊" to "开心", "😐" to "平淡", "😢" to "难过", "😡" to "生气", "🤩" to "兴奋", "😴" to "疲惫")

    AlertDialog(
        onDismissRequest = onDismiss, shape = RoundedCornerShape(4.dp), containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (existing == null) "写日记" else DateUtils.formatDate(existing!!.date)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("标题") }, singleLine = true, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    moods.forEach { (emoji, label) ->
                        FilterChip(emoji == mood, { mood = if (mood == emoji) "" else emoji }, { Text(emoji) })
                    }
                }
                OutlinedTextField(content, { content = it }, label = { Text("内容") }, minLines = 5, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onSave(DiaryEntry(id = existing?.id ?: 0, date = existing?.date ?: DateUtils.todayTimestamp(), title = title, content = content, mood = mood)) }) { Text("保存", color = MaterialTheme.colorScheme.onSurface) } },
        dismissButton = {
            Row {
                if (existing != null) TextButton(onClick = onDelete) { Text("删除", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    )
}

@Composable
private fun EmptyMsg(title: String, onAdd: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.EditNote, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
