package com.gk.diaguide.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import com.gk.diaguide.domain.model.GlucoseUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onUpdate: ((OnboardingUiState) -> OnboardingUiState) -> Unit,
    onSave: () -> Unit,
    showScaffold: Boolean = true,
) {
    val content: @Composable (PaddingValues) -> Unit = { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.onboarding_intro),
                style = MaterialTheme.typography.bodyMedium,
            )

            OutlinedTextField(
                value = state.displayName,
                onValueChange = { value -> onUpdate { it.copy(displayName = value) } },
                label = { Text(stringResource(R.string.onboarding_name_label)) },
                modifier = Modifier,
            )
            OutlinedTextField(
                value = state.diabetesType,
                onValueChange = { value -> onUpdate { it.copy(diabetesType = value) } },
                label = { Text(stringResource(R.string.onboarding_diabetes_type_label)) },
            )
            OutlinedTextField(
                value = state.ageGroup,
                onValueChange = { value -> onUpdate { it.copy(ageGroup = value) } },
                label = { Text(stringResource(R.string.onboarding_age_group_label)) },
            )
            UnitSelector(
                selected = state.unit,
                onSelected = { unit -> onUpdate { it.copy(unit = unit) } },
            )

            ThresholdField(stringResource(R.string.threshold_target_low), state.targetLow) { value -> onUpdate { it.copy(targetLow = value) } }
            ThresholdField(stringResource(R.string.threshold_target_high), state.targetHigh) { value -> onUpdate { it.copy(targetHigh = value) } }
            ThresholdField(stringResource(R.string.threshold_warning_low), state.warningLow) { value -> onUpdate { it.copy(warningLow = value) } }
            ThresholdField(stringResource(R.string.threshold_warning_high), state.warningHigh) { value -> onUpdate { it.copy(warningHigh = value) } }
            ThresholdField(stringResource(R.string.threshold_critical_low), state.criticalLow) { value -> onUpdate { it.copy(criticalLow = value) } }
            ThresholdField(stringResource(R.string.threshold_critical_high), state.criticalHigh) { value -> onUpdate { it.copy(criticalHigh = value) } }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(
                    checked = state.disclaimerAccepted,
                    onCheckedChange = { checked -> onUpdate { it.copy(disclaimerAccepted = checked) } },
                )
                Text(
                    stringResource(R.string.onboarding_disclaimer_text),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(onClick = onSave, contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)) {
                Text(stringResource(R.string.onboarding_continue))
            }
        }
    }
    if (showScaffold) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(stringResource(R.string.onboarding_title)) }) },
        ) { padding -> content(padding) }
    } else {
        content(PaddingValues(0.dp))
    }
}

@Composable
private fun ThresholdField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitSelector(
    selected: GlucoseUnit,
    onSelected: (GlucoseUnit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(
                when (selected) {
                    GlucoseUnit.MG_DL -> R.string.unit_mg_dl
                    GlucoseUnit.MMOL_L -> R.string.unit_mmol_l
                },
            ),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.onboarding_unit_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.colors(),
            modifier = Modifier.menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            GlucoseUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(
                                when (unit) {
                                    GlucoseUnit.MG_DL -> R.string.unit_mg_dl
                                    GlucoseUnit.MMOL_L -> R.string.unit_mmol_l
                                },
                            ),
                        )
                    },
                    onClick = {
                        onSelected(unit)
                        expanded = false
                    },
                )
            }
        }
    }
}
