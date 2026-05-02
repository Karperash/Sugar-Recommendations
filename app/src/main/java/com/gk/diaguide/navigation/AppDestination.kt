package com.gk.diaguide.navigation

import androidx.annotation.StringRes
import com.gk.diaguide.R

sealed class AppDestination(
    val route: String,
    val title: String,
    @StringRes val titleResId: Int = 0,
    val showBottomBar: Boolean = false,
) {
    data object Splash : AppDestination("splash", "", R.string.nav_splash)
    data object Onboarding : AppDestination("onboarding", "", R.string.onboarding_title)
    data object Dashboard : AppDestination("dashboard", "", R.string.nav_dashboard, showBottomBar = true)
    data object Nutrition : AppDestination("nutrition", "", R.string.nav_nutrition, showBottomBar = true)
    data object History : AppDestination("history", "", R.string.nav_history)
    data object Recommendations : AppDestination("recommendations", "", R.string.nav_recommendations, showBottomBar = true)
    data object EventLog : AppDestination("events", "", R.string.nav_glucose_records, showBottomBar = true)
    data object ManualEntry : AppDestination("manual", "", R.string.manual_title)
    data object Import : AppDestination("import", "", R.string.import_title)
    data object Settings : AppDestination("settings", "", R.string.nav_settings, showBottomBar = true)
    data object About : AppDestination("about", "", R.string.about_title)
}

val bottomDestinations = listOf(
    AppDestination.Dashboard,
    AppDestination.EventLog,
    AppDestination.Nutrition,
    AppDestination.Recommendations,
    AppDestination.Settings,
)
