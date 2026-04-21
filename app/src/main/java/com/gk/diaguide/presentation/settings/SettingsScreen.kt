package com.gk.diaguide.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gk.diaguide.R
import com.gk.diaguide.core.util.shortLabelRu
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.navigation.AppDestination
import com.gk.diaguide.presentation.onboarding.OnboardingScreen
import com.gk.diaguide.presentation.onboarding.OnboardingUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settingsIntroCompleted: Boolean,
    introState: OnboardingUiState,
    onIntroUpdate: ((OnboardingUiState) -> OnboardingUiState) -> Unit,
    onCompleteIntro: () -> Unit,
    state: SettingsUiState,
    onUpdate: ((SettingsUiState) -> SettingsUiState) -> Unit,
    onSave: () -> Unit,
    onNavigate: (AppDestination) -> Unit,
    onApplyZenodoPreset: () -> Unit,
    onApplyOhioPreset: () -> Unit,
) {
    if (!settingsIntroCompleted) {
        Box(Modifier.fillMaxSize()) {
            OnboardingScreen(
                state = introState,
                onUpdate = onIntroUpdate,
                onSave = onCompleteIntro,
                showScaffold = true,
            )
        }
        return
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.settings_profile), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.displayName,
                onValueChange = { value -> onUpdate { it.copy(displayName = value) } },
                label = { Text(stringResource(R.string.settings_name_label)) },
            )
            OutlinedTextField(
                value = state.diabetesType,
                onValueChange = { value -> onUpdate { it.copy(diabetesType = value) } },
                label = { Text(stringResource(R.string.settings_diabetes_type_label)) },
            )
            OutlinedTextField(
                value = state.ageGroup,
                onValueChange = { value -> onUpdate { it.copy(ageGroup = value) } },
                label = { Text(stringResource(R.string.settings_age_group_label)) },
            )

            Text(stringResource(R.string.settings_glucose_unit))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlucoseUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = unit == state.unit,
                        onClick = { onUpdate { it.copy(unit = unit) } },
                        label = { Text(unit.shortLabelRu()) },
                    )
                }
            }

            Text(stringResource(R.string.settings_reco_presets), style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = onApplyZenodoPreset) {
                Text(stringResource(R.string.settings_preset_zenodo))
            }
            OutlinedButton(onClick = onApplyOhioPreset) {
                Text(stringResource(R.string.settings_preset_ohio))
            }

            SettingField(stringResource(R.string.threshold_target_low), state.targetLow) { value -> onUpdate { it.copy(targetLow = value) } }
            SettingField(stringResource(R.string.threshold_target_high), state.targetHigh) { value -> onUpdate { it.copy(targetHigh = value) } }
            SettingField(stringResource(R.string.threshold_warning_low), state.warningLow) { value -> onUpdate { it.copy(warningLow = value) } }
            SettingField(stringResource(R.string.threshold_warning_high), state.warningHigh) { value -> onUpdate { it.copy(warningHigh = value) } }
            SettingField(stringResource(R.string.threshold_critical_low), state.criticalLow) { value -> onUpdate { it.copy(criticalLow = value) } }
            SettingField(stringResource(R.string.threshold_critical_high), state.criticalHigh) { value -> onUpdate { it.copy(criticalHigh = value) } }
            SettingField(stringResource(R.string.threshold_rapid_rise), state.rapidRise) { value -> onUpdate { it.copy(rapidRise = value) } }
            SettingField(stringResource(R.string.threshold_rapid_fall), state.rapidFall) { value -> onUpdate { it.copy(rapidFall = value) } }
            SettingField(stringResource(R.string.threshold_prolonged_minutes), state.prolongedMinutes) { value -> onUpdate { it.copy(prolongedMinutes = value) } }
            SettingField(stringResource(R.string.threshold_pattern_window_hours), state.patternWindowHours) { value -> onUpdate { it.copy(patternWindowHours = value) } }

            Text(stringResource(R.string.settings_reminders), style = MaterialTheme.typography.titleMedium)
            SettingField(stringResource(R.string.settings_reminder_interval), state.reminderIntervalHours) { value -> onUpdate { it.copy(reminderIntervalHours = value) } }

            state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            Button(onClick = onSave) { Text(stringResource(R.string.settings_save)) }
            Button(onClick = { onNavigate(AppDestination.About) }) { Text(stringResource(R.string.settings_open_disclaimer)) }
        }
    }
}

@Composable
private fun SettingField(
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
