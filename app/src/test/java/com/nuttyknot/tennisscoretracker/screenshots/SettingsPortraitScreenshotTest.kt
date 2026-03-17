package com.nuttyknot.tennisscoretracker.screenshots

import android.view.KeyEvent
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.MatchFormat
import com.nuttyknot.tennisscoretracker.ui.settings.AppSettingsData
import com.nuttyknot.tennisscoretracker.ui.settings.LatencySettingsData
import com.nuttyknot.tennisscoretracker.ui.settings.PlayerSettingsData
import com.nuttyknot.tennisscoretracker.ui.settings.SettingsScreenPreview
import com.nuttyknot.tennisscoretracker.ui.theme.TennisScoreTrackerTheme
import org.junit.Rule
import org.junit.Test

class SettingsPortraitScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_6,
            theme = "android:Theme.Material.NoActionBar.Fullscreen",
            renderingMode = SessionParams.RenderingMode.NORMAL,
        )

    @Test
    fun defaultSettings() {
        val playerData =
            PlayerSettingsData(
                userName = "",
                opponentName = "",
                initialServerIsUser = true,
                onUserNameChange = {},
                onOpponentNameChange = {},
                onInitialServerChange = {},
            )
        val appData =
            AppSettingsData(
                currentKeycode = KeyEvent.KEYCODE_VOLUME_UP,
                currentTheme = AppTheme.SKY_BLUE,
                currentMatchFormat = MatchFormat.STANDARD,
                ttsEnabled = true,
                onKeycodeChange = {},
                onThemeChange = {},
                onMatchFormatChange = {},
                onTtsEnabledChange = {},
            )
        val latencyData =
            LatencySettingsData(
                currentDoubleClick = 300L,
                currentLongPress = 1000L,
                onDoubleClickChange = {},
                onLongPressChange = {},
            )
        paparazzi.snapshot("defaultSettings") {
            TennisScoreTrackerTheme(appTheme = AppTheme.SKY_BLUE) {
                SettingsScreenPreview(playerData, appData, latencyData)
            }
        }
    }
}
