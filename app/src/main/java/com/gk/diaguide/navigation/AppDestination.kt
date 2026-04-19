package com.gk.diaguide.navigation

import androidx.annotation.StringRes
import com.gk.diaguide.R

sealed class AppDestination(
    val route: String,
    val title: String,
    @StringRes val titleResId: Int = 0,
    val showBottomBar: Boolean = false,
) {
    data object Splash : AppDestination("splash", "Loading")
    data object Onboarding : AppDestination("onboarding", "Onboarding")
    data object Dashboard : AppDestination("dashboard", "Dashboard", R.string.nav_dashboard, showBottomBar = true)
    data object Chart : AppDestination("chart", "CGM Chart", R.string.nav_chart, showBottomBar = true)
    data object History : AppDestination("history", "History", R.string.nav_history, showBottomBar = true)
    data object Recommendations : AppDestination("recommendations", "Recommendations", R.string.nav_recommendations, showBottomBar = true)
    data object EventLog : AppDestination("events", "Event Log")
    data object ManualEntry : AppDestination("manual", "Manual Entry")
    data object Import : AppDestination("import", "Import Data")
    data object Settings : AppDestination("settings", "Settings", R.string.nav_settings, showBottomBar = true)
    data object About : AppDestination("about", "About")
}

val bottomDestinations = listOf(
    AppDestination.Dashboard,
    AppDestination.Chart,
    AppDestination.History,
    AppDestination.Recommendations,
    AppDestination.Settings,
)
