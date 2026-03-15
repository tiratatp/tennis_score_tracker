package com.nuttyknot.tennisscoretracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nuttyknot.tennisscoretracker.ScoreManager
import com.nuttyknot.tennisscoretracker.SettingsManager
import kotlinx.coroutines.launch

@Suppress("FunctionNaming", "ktlint:standard:function-naming")
@Composable
fun TennisAppNavigation(
    navController: NavHostController = rememberNavController(),
    scoreManager: ScoreManager,
    settingsManager: SettingsManager,
) {
    val hasSeenHelp by settingsManager.hasSeenHelpFlow.collectAsState(initial = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(hasSeenHelp) {
        if (!hasSeenHelp) {
            navController.navigate(Routes.HELP_SCREEN) {
                popUpTo(Routes.SCORE_SCREEN) { inclusive = false }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SCORE_SCREEN,
    ) {
        composable(Routes.SCORE_SCREEN) {
            ScoreScreen(
                scoreManager = scoreManager,
                settingsManager = settingsManager,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS_SCREEN) },
                onNavigateToHelp = { navController.navigate(Routes.HELP_SCREEN) },
            )
        }
        composable(Routes.SETTINGS_SCREEN) {
            SettingsScreen(
                settingsManager = settingsManager,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Routes.HELP_SCREEN) {
            HelpScreen(
                onDismiss = {
                    scope.launch {
                        settingsManager.updateHasSeenHelp(true)
                    }
                    navController.popBackStack()
                },
            )
        }
    }
}
