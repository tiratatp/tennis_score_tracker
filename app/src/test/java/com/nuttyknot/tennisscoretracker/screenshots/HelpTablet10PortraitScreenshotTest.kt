package com.nuttyknot.tennisscoretracker.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.ScreenOrientation
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.ui.help.HelpScreen
import com.nuttyknot.tennisscoretracker.ui.theme.TennisScoreTrackerTheme
import org.junit.Rule
import org.junit.Test

class HelpTablet10PortraitScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_C.copy(orientation = ScreenOrientation.PORTRAIT),
            theme = "android:Theme.Material.NoActionBar.Fullscreen",
            renderingMode = SessionParams.RenderingMode.NORMAL,
        )

    @Test
    fun help() {
        paparazzi.snapshot("help") {
            TennisScoreTrackerTheme(appTheme = AppTheme.SKY_BLUE) {
                HelpScreen(onDismiss = {})
            }
        }
    }
}
