package com.nuttyknot.tennisscoretracker.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.BuildConfig
import com.nuttyknot.tennisscoretracker.ui.score.ScoreScreenPreview
import com.nuttyknot.tennisscoretracker.ui.theme.TennisScoreTrackerTheme
import org.junit.Rule
import org.junit.Test

class ScoreTablet7ScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig =
                DeviceConfig.NEXUS_7.copy(
                    screenWidth = 1080,
                    screenHeight = 1920,
                    density = Density.XHIGH,
                    orientation = ScreenOrientation.LANDSCAPE,
                ),
            theme = "android:Theme.Material.NoActionBar.Fullscreen",
            renderingMode = SessionParams.RenderingMode.NORMAL,
            useDeviceResolution = true,
        )

    @Test
    fun inmatch() {
        val state = SportTestData.inMatchForSport(BuildConfig.SPORT)
        paparazzi.snapshot("inmatch") {
            TennisScoreTrackerTheme(appTheme = AppTheme.GRAND_SLAM) {
                ScoreScreenPreview(state)
            }
        }
    }
}
