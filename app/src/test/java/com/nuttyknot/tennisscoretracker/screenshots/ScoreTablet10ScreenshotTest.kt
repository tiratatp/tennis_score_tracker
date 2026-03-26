package com.nuttyknot.tennisscoretracker.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import com.nuttyknot.tennisscoretracker.AppTheme
import com.nuttyknot.tennisscoretracker.MatchState
import com.nuttyknot.tennisscoretracker.PlayerScore
import com.nuttyknot.tennisscoretracker.ui.score.ScoreScreenPreview
import com.nuttyknot.tennisscoretracker.ui.theme.TennisScoreTrackerTheme
import org.junit.Rule
import org.junit.Test

// 2023 US Open Final: Gauff d. Sabalenka 2-6, 6-3, 6-2
class ScoreTablet10ScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig =
                DeviceConfig.PIXEL_C.copy(
                    screenWidth = 1080,
                    screenHeight = 1920,
                    density = Density.HIGH,
                    orientation = ScreenOrientation.LANDSCAPE,
                ),
            theme = "android:Theme.Material.NoActionBar.Fullscreen",
            renderingMode = SessionParams.RenderingMode.NORMAL,
            useDeviceResolution = true,
        )

    @Test
    fun inmatch() {
        // 3rd set, Gauff serving at 40-15, games 5-2
        val state =
            MatchState(
                userScore = PlayerScore.Forty,
                opponentScore = PlayerScore.Fifteen,
                userGames = 5,
                opponentGames = 2,
                userSets = 1,
                opponentSets = 1,
                setHistory = listOf(2 to 6, 6 to 3),
                isUserServing = true,
                userName = "Gauff",
                opponentName = "Sabalenka",
            )
        paparazzi.snapshot("inmatch") {
            TennisScoreTrackerTheme(appTheme = AppTheme.MIAMI_NIGHT) {
                ScoreScreenPreview(state)
            }
        }
    }
}
