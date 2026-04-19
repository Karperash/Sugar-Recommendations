package com.gk.diaguide.presentation.manual

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gk.diaguide.R
import com.gk.diaguide.domain.model.TrendDirection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManualEntryScreen(
    state: ManualEntryUiState,
    onUpdate: ((ManualEntryUiState) -> ManualEntryUiState) -> Unit,
    onSave: () -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.manual_title)) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.glucoseValue,
                onValueChange = { value -> onUpdate { it.copy(glucoseValue = value) } },
                label = { Text(stringResource(R.string.manual_glucose_value)) },
            )
            TrendSelector(
                selected = state.trendDirection,
                onSelected = { direction -> onUpdate { it.copy(trendDirection = direction) } },
            )
            OutlinedTextField(
                value = state.note,
                onValueChange = { value -> onUpdate { it.copy(note = value) } },
                label = { Text(stringResource(R.string.manual_note_label)) },
                minLines = 3,
            )

            Text(stringResource(R.string.manual_tags_title), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TagChip(stringResource(R.string.manual_tag_meal), state.meal) { onUpdate { it.copy(meal = !it.meal) } }
                TagChip(stringResource(R.string.manual_tag_insulin), state.insulin) { onUpdate { it.copy(insulin = !it.insulin) } }
                TagChip(stringResource(R.string.manual_tag_activity), state.activity) { onUpdate { it.copy(activity = !it.activity) } }
                TagChip(stringResource(R.string.manual_tag_sleep), state.sleep) { onUpdate { it.copy(sleep = !it.sleep) } }
                TagChip(stringResource(R.string.manual_tag_stress), state.stress) { onUpdate { it.copy(stress = !it.stress) } }
                TagChip(stringResource(R.string.manual_tag_symptom), state.symptom) { onUpdate { it.copy(symptom = !it.symptom) } }
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(onClick = onSave) { Text(stringResource(R.string.manual_save)) }
        }
    }
}

@Composable
private fun TagChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrendSelector(
    selected: TrendDirection,
    onSelected: (TrendDirection) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.manual_trend_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.colors(),
            modifier = Modifier.menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TrendDirection.entries.forEach { direction ->
                DropdownMenuItem(
                    text = { Text(direction.name) },
                    onClick = {
                        onSelected(direction)
                        expanded = false
                    },
                )
            }
        }
    }
}
