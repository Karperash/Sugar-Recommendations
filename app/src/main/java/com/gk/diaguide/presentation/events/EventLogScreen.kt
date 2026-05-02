package com.gk.diaguide.presentation.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gk.diaguide.R
import com.gk.diaguide.core.ui.entrySourceLabel
import com.gk.diaguide.core.util.formatDateTime
import com.gk.diaguide.core.util.formatGlucose
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.domain.model.toUnit
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventLogScreen(
    state: EventLogUiState,
    onUpdateEntry: (CgmRecord) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onNavigateHome: () -> Unit,
    onClearAllData: () -> Unit,
) {
    var editor by remember { mutableStateOf<RecordEditor?>(null) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_glucose_records)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.events_back_home_cd),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showClearAllConfirm = true }) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteForever,
                            contentDescription = stringResource(R.string.events_reset_data_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.events_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (state.records.isEmpty()) {
                item { Text(stringResource(R.string.events_empty)) }
            }
            items(state.records, key = { it.id }) { record ->
                Card(onClick = { editor = RecordEditor.open(record, state.settings) }) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            record.toUnit(state.settings.glucoseUnit).glucoseValue.formatGlucose(state.settings.glucoseUnit),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(record.timestamp.formatDateTime(), style = MaterialTheme.typography.bodySmall)
                        Text(entrySourceLabel(record.source), style = MaterialTheme.typography.bodySmall)
                        record.note?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }

    editor?.let { draft ->
        AlertDialog(
            onDismissRequest = { editor = null },
            title = { Text(stringResource(R.string.events_edit_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = draft.glucoseText,
                        onValueChange = { editor = draft.copy(glucoseText = it) },
                        label = { Text(stringResource(R.string.manual_glucose_value)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = draft.timestampText,
                        onValueChange = { editor = draft.copy(timestampText = it) },
                        label = { Text(stringResource(R.string.events_timestamp_label)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = draft.note,
                        onValueChange = { editor = draft.copy(note = it) },
                        label = { Text(stringResource(R.string.manual_note_label)) },
                        minLines = 2,
                    )
                    Text(stringResource(R.string.manual_tags_title), style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = draft.meal, onCheckedChange = { editor = draft.copy(meal = it) })
                        Text(stringResource(R.string.manual_tag_meal))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = draft.insulin, onCheckedChange = { editor = draft.copy(insulin = it) })
                        Text(stringResource(R.string.manual_tag_insulin))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = draft.activity, onCheckedChange = { editor = draft.copy(activity = it) })
                        Text(stringResource(R.string.manual_tag_activity))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = draft.sleep, onCheckedChange = { editor = draft.copy(sleep = it) })
                        Text(stringResource(R.string.manual_tag_sleep))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = draft.stress, onCheckedChange = { editor = draft.copy(stress = it) })
                        Text(stringResource(R.string.manual_tag_stress))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = draft.symptom, onCheckedChange = { editor = draft.copy(symptom = it) })
                        Text(stringResource(R.string.manual_tag_symptom))
                    }
                    draft.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                val invalidGlucose = stringResource(R.string.manual_error_invalid_value)
                val invalidTs = stringResource(R.string.events_error_timestamp)
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { pendingDeleteId = draft.base.id }) {
                        Text(stringResource(R.string.events_delete))
                    }
                    TextButton(
                        onClick = {
                            val zone = ZoneId.systemDefault()
                            val parsedTs = draft.parseTimestamp(zone)
                            val glucose = draft.glucoseText.replace(',', '.').trim().toDoubleOrNull()
                            if (glucose == null) {
                                editor = draft.copy(error = invalidGlucose)
                                return@TextButton
                            }
                            if (parsedTs == null) {
                                editor = draft.copy(error = invalidTs)
                                return@TextButton
                            }
                            onUpdateEntry(
                                draft.base.copy(
                                    timestamp = parsedTs,
                                    glucoseValue = glucose,
                                    unit = state.settings.glucoseUnit,
                                    note = draft.note.takeIf { it.isNotBlank() },
                                    meal = draft.meal,
                                    insulin = draft.insulin,
                                    activity = draft.activity,
                                    sleep = draft.sleep,
                                    stress = draft.stress,
                                    symptom = draft.symptom,
                                ),
                            )
                            editor = null
                        },
                    ) { Text(stringResource(R.string.events_save)) }
                }
            },
            dismissButton = {
                TextButton(onClick = { editor = null }) { Text(stringResource(R.string.events_cancel)) }
            },
        )
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(stringResource(R.string.events_delete)) },
            text = { Text(stringResource(R.string.events_delete_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEntry(id)
                        pendingDeleteId = null
                        editor = null
                    },
                ) { Text(stringResource(R.string.events_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text(stringResource(R.string.events_cancel)) }
            },
        )
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text(stringResource(R.string.events_clear_all_title)) },
            text = { Text(stringResource(R.string.events_clear_all_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllData()
                        showClearAllConfirm = false
                        editor = null
                    },
                ) { Text(stringResource(R.string.events_clear_all_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text(stringResource(R.string.events_cancel))
                }
            },
        )
    }
}

private data class RecordEditor(
    val base: CgmRecord,
    val glucoseText: String,
    val timestampText: String,
    val note: String,
    val meal: Boolean,
    val insulin: Boolean,
    val activity: Boolean,
    val sleep: Boolean,
    val stress: Boolean,
    val symptom: Boolean,
    val error: String? = null,
) {
    fun parseTimestamp(zoneId: ZoneId) = runCatching {
        val fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        LocalDateTime.parse(timestampText.trim(), fmt).atZone(zoneId).toInstant()
    }.getOrNull()

    companion object {
        fun open(record: CgmRecord, settings: UserSettings): RecordEditor {
            val display = record.toUnit(settings.glucoseUnit)
            val text = when (settings.glucoseUnit) {
                GlucoseUnit.MG_DL -> String.format(Locale.US, "%.0f", display.glucoseValue)
                GlucoseUnit.MMOL_L -> String.format(Locale.US, "%.1f", display.glucoseValue)
            }
            return RecordEditor(
                base = record,
                glucoseText = text,
                timestampText = record.timestamp.formatDateTime(),
                note = record.note.orEmpty(),
                meal = record.meal,
                insulin = record.insulin,
                activity = record.activity,
                sleep = record.sleep,
                stress = record.stress,
                symptom = record.symptom,
            )
        }
    }
}
