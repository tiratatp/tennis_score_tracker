package com.nuttyknot.tennisscoretracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.nuttyknot.tennisscoretracker.AppTheme

private val GrandSlamColorScheme =
    darkColorScheme(
        primary = GrandSlamYellow,
        secondary = GrandSlamWhite,
        background = Black,
        surface = Black,
        onPrimary = Black,
        onSecondary = Black,
        onBackground = White,
        onSurface = White,
    )

private val MiamiNightColorScheme =
    darkColorScheme(
        primary = MiamiCyan,
        secondary = MiamiMagenta,
        background = Black,
        surface = Black,
        onPrimary = Black,
        onSecondary = Black,
        onBackground = White,
        onSurface = White,
    )

private val ColorblindSafeColorScheme =
    darkColorScheme(
        primary = ColorblindOrange,
        secondary = ColorblindBlue,
        background = Black,
        surface = Black,
        onPrimary = Black,
        onSecondary = Black,
        onBackground = White,
        onSurface = White,
    )

private val SkyBlueColorScheme =
    darkColorScheme(
        primary = SkyBlue,
        secondary = White,
        background = Black,
        surface = Black,
        onPrimary = Black,
        onSecondary = Black,
        onBackground = White,
        onSurface = White,
    )

@Suppress("FunctionName")
@Composable
fun TennisScoreTrackerTheme(
    appTheme: AppTheme = AppTheme.SKY_BLUE,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when (appTheme) {
            AppTheme.GRAND_SLAM -> GrandSlamColorScheme
            AppTheme.MIAMI_NIGHT -> MiamiNightColorScheme
            AppTheme.COLORBLIND_SAFE -> ColorblindSafeColorScheme
            AppTheme.SKY_BLUE -> SkyBlueColorScheme
        }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
