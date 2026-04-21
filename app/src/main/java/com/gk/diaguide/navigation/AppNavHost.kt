package com.gk.diaguide.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import com.gk.diaguide.core.ui.AppBottomBar
import com.gk.diaguide.presentation.about.AboutScreen
import com.gk.diaguide.presentation.chart.ChartScreen
import com.gk.diaguide.presentation.chart.ChartViewModel
import com.gk.diaguide.presentation.dashboard.DashboardScreen
import com.gk.diaguide.presentation.dashboard.DashboardViewModel
import com.gk.diaguide.presentation.events.EventLogScreen
import com.gk.diaguide.presentation.events.EventLogViewModel
import com.gk.diaguide.presentation.history.HistoryScreen
import com.gk.diaguide.presentation.history.HistoryViewModel
import com.gk.diaguide.presentation.imports.ImportScreen
import com.gk.diaguide.presentation.imports.ImportViewModel
import com.gk.diaguide.presentation.manual.ManualEntryScreen
import com.gk.diaguide.presentation.manual.ManualEntryViewModel
import com.gk.diaguide.presentation.nutrition.NutritionScreen
import com.gk.diaguide.presentation.recommendations.RecommendationsScreen
import com.gk.diaguide.presentation.recommendations.RecommendationsViewModel
import com.gk.diaguide.presentation.settings.SettingsScreen
import com.gk.diaguide.presentation.settings.SettingsViewModel
import com.gk.diaguide.presentation.splash.SplashScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    val destination = listOf(
        AppDestination.Dashboard,
        AppDestination.Chart,
        AppDestination.Nutrition,
        AppDestination.History,
        AppDestination.Recommendations,
        AppDestination.Settings,
        AppDestination.ManualEntry,
        AppDestination.Import,
        AppDestination.EventLog,
        AppDestination.About,
        AppDestination.Splash,
    ).firstOrNull { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (destination?.showBottomBar == true) {
                AppBottomBar(currentRoute = currentRoute) { target ->
                    navController.navigate(target.route) {
                        popUpTo(AppDestination.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Splash.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) },
        ) {
            composable(AppDestination.Splash.route) {
                LaunchedEffect(Unit) {
                    navController.navigate(AppDestination.Dashboard.route) {
                        popUpTo(AppDestination.Splash.route) { inclusive = true }
                    }
                }
                SplashScreen()
            }

            composable(AppDestination.Dashboard.route) {
                val viewModel: DashboardViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                DashboardScreen(
                    state = state,
                    onNavigate = { navController.navigate(it.route) },
                    onRefresh = viewModel::refresh,
                )
            }

            composable(AppDestination.Chart.route) {
                val viewModel: ChartViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                ChartScreen(state = state, onRangeSelected = viewModel::setRange)
            }

            composable(AppDestination.Nutrition.route) {
                NutritionScreen()
            }

            composable(AppDestination.History.route) {
                val viewModel: HistoryViewModel = hiltViewModel()
                val records by viewModel.records.collectAsStateWithLifecycle()
                HistoryScreen(records)
            }

            composable(AppDestination.Recommendations.route) {
                val viewModel: RecommendationsViewModel = hiltViewModel()
                val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
                RecommendationsScreen(recommendations)
            }

            composable(
                AppDestination.EventLog.route,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },
            ) {
                val viewModel: EventLogViewModel = hiltViewModel()
                val events by viewModel.events.collectAsStateWithLifecycle()
                EventLogScreen(events)
            }

            composable(
                AppDestination.ManualEntry.route,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },
            ) {
                val viewModel: ManualEntryViewModel = hiltViewModel()
                LaunchedEffect(Unit) {
                    viewModel.saved.collectLatest {
                        navController.popBackStack()
                    }
                }
                ManualEntryScreen(
                    state = viewModel.uiState,
                    onUpdate = viewModel::update,
                    onSave = viewModel::save,
                )
            }

            composable(
                AppDestination.Import.route,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },
            ) {
                val viewModel: ImportViewModel = hiltViewModel()
                ImportScreen(
                    state = viewModel.uiState,
                    onImportCsv = viewModel::importCsv,
                    onImportJson = viewModel::importJson,
                    onLoadScenario = viewModel::loadScenario,
                )
            }

            composable(AppDestination.Settings.route) {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    settingsIntroCompleted = viewModel.settingsIntroCompleted,
                    introState = viewModel.introUiState,
                    onIntroUpdate = viewModel::updateIntro,
                    onCompleteIntro = viewModel::completeFirstTimeIntro,
                    state = viewModel.uiState,
                    onUpdate = viewModel::update,
                    onSave = viewModel::save,
                    onNavigate = { navController.navigate(it.route) },
                    onApplyZenodoPreset = viewModel::applyZenodoT1dUomPreset,
                    onApplyOhioPreset = viewModel::applyOhioResearchPreset,
                )
            }

            composable(
                AppDestination.About.route,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },
            ) {
                AboutScreen()
            }
        }
    }
}
