package com.nuttyknot.tennisscoretracker.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as ComponentActivity
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
