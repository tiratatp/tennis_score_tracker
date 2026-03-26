package com.nuttyknot.tennisscoretracker.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.ScreenOrientation
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.BuildConfig
import com.nuttyknot.tennisscoretracker.ui.score.ScoreScreenPreview
import com.nuttyknot.tennisscoretracker.ui.theme.TennisScoreTrackerTheme
import org.junit.Rule
import org.junit.Test

class ScoreLandscapeScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.NEXUS_5.copy(orientation = ScreenOrientation.LANDSCAPE),
            theme = "android:Theme.Material.NoActionBar.Fullscreen",
            renderingMode = SessionParams.RenderingMode.NORMAL,
            useDeviceResolution = true,
        )

    @Test
    fun matchOver() {
        val state = SportTestData.matchOverForSport(BuildConfig.SPORT)
        paparazzi.snapshot("matchOver") {
            TennisScoreTrackerTheme(appTheme = AppTheme.SKY_BLUE) {
                ScoreScreenPreview(state)
            }
        }
    }

    @Test
    fun inMatch() {
        val state = SportTestData.inMatchForSport(BuildConfig.SPORT)
        paparazzi.snapshot("inMatch") {
            TennisScoreTrackerTheme(appTheme = AppTheme.SKY_BLUE) {
                ScoreScreenPreview(state)
            }
        }
    }
}
