package com.gk.diaguide.presentation.imports

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gk.diaguide.R
import com.gk.diaguide.data.mock.ScenarioType

@Composable
fun scenarioTypeLabel(type: ScenarioType): String = stringResource(
    when (type) {
        ScenarioType.STABLE_DAY -> R.string.scenario_stable_day
        ScenarioType.REPEATED_HIGH -> R.string.scenario_repeated_high
        ScenarioType.REPEATED_LOW -> R.string.scenario_repeated_low
        ScenarioType.SHARP_FLUCTUATIONS -> R.string.scenario_sharp_fluctuations
        ScenarioType.NIGHT_EPISODES -> R.string.scenario_night_episodes
    },
)

fun scenarioTypeTitleRes(type: ScenarioType): Int = when (type) {
    ScenarioType.STABLE_DAY -> R.string.scenario_stable_day
    ScenarioType.REPEATED_HIGH -> R.string.scenario_repeated_high
    ScenarioType.REPEATED_LOW -> R.string.scenario_repeated_low
    ScenarioType.SHARP_FLUCTUATIONS -> R.string.scenario_sharp_fluctuations
    ScenarioType.NIGHT_EPISODES -> R.string.scenario_night_episodes
}
