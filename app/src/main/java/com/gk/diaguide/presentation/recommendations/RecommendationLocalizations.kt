package com.gk.diaguide.presentation.recommendations

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gk.diaguide.R
import com.gk.diaguide.domain.model.PatternType
import com.gk.diaguide.domain.model.Recommendation

private fun Recommendation.patternType(): PatternType? =
    PatternType.entries.find { it.name.equals(relatedDetectedPattern, ignoreCase = true) }

@Composable
fun Recommendation.displayTitle(): String {
    return when (patternType()) {
        PatternType.CRITICAL_LOW -> stringResource(R.string.reco_title_critical_low)
        PatternType.CRITICAL_HIGH -> stringResource(R.string.reco_title_critical_high)
        PatternType.LOW_VALUE -> stringResource(R.string.reco_title_low_value)
        PatternType.HIGH_VALUE -> stringResource(R.string.reco_title_high_value)
        PatternType.RAPID_RISE -> stringResource(R.string.reco_title_rapid_rise)
        PatternType.RAPID_FALL -> stringResource(R.string.reco_title_rapid_fall)
        PatternType.PROLONGED_OUT_OF_RANGE -> stringResource(R.string.reco_title_prolonged)
        PatternType.REPEATED_DEVIATIONS -> stringResource(R.string.reco_title_repeated)
        PatternType.MORNING_HIGHS -> stringResource(R.string.reco_title_morning_highs)
        PatternType.POST_MEAL_SPIKE -> stringResource(R.string.reco_title_post_meal)
        PatternType.NIGHT_LOW -> stringResource(R.string.reco_title_night_low)
        null -> title
    }
}

@Composable
fun Recommendation.displayExplanation(): String {
    return when (patternType()) {
        PatternType.CRITICAL_LOW -> stringResource(R.string.reco_body_critical_low)
        PatternType.CRITICAL_HIGH -> stringResource(R.string.reco_body_critical_high)
        PatternType.LOW_VALUE -> stringResource(R.string.reco_body_low_value)
        PatternType.HIGH_VALUE -> stringResource(R.string.reco_body_high_value)
        PatternType.RAPID_RISE -> stringResource(R.string.reco_body_rapid_rise)
        PatternType.RAPID_FALL -> stringResource(R.string.reco_body_rapid_fall)
        PatternType.PROLONGED_OUT_OF_RANGE -> stringResource(R.string.reco_body_prolonged)
        PatternType.REPEATED_DEVIATIONS -> stringResource(R.string.reco_body_repeated)
        PatternType.MORNING_HIGHS -> stringResource(R.string.reco_body_morning_highs)
        PatternType.POST_MEAL_SPIKE -> stringResource(R.string.reco_body_post_meal)
        PatternType.NIGHT_LOW -> stringResource(R.string.reco_body_night_low)
        null -> shortExplanation
    }
}
