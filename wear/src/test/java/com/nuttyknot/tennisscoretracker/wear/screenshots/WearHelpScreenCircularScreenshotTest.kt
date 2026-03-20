package com.nuttyknot.tennisscoretracker.wear.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.Density
import com.android.resources.ScreenRatio
import com.android.resources.ScreenRound
import com.android.resources.ScreenSize
import com.nuttyknot.tennisscoretracker.shared.WearScoreDisplay
import com.nuttyknot.tennisscoretracker.wear.ui.WearScoreScreen
import com.nuttyknot.tennisscoretracker.wear.ui.WearTheme
import org.junit.Rule
import org.junit.Test

// Circular variant for UX/internal testing on round watch displays
class WearHelpScreenCircularScreenshotTest {
    companion object {
        // Galaxy Watch 7 (44mm): 480x480 px, ~327 PPI, round
        private val GALAXY_WATCH_7_ROUND =
            DeviceConfig(
                screenWidth = 480,
                screenHeight = 480,
                density = Density.XHIGH,
                ratio = ScreenRatio.NOTLONG,
                size = ScreenSize.SMALL,
                screenRound = ScreenRound.ROUND,
            )
    }

    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = GALAXY_WATCH_7_ROUND,
            theme = "android:Theme.DeviceDefault",
            renderingMode = SessionParams.RenderingMode.NORMAL,
        )

    @Test
    fun help() {
        val score =
            WearScoreDisplay(
                userName = "Sharapova",
                opponentName = "Williams",
                userScore = "40",
                opponentScore = "30",
                userGames = 5,
                opponentGames = 4,
                userSets = 1,
                opponentSets = 0,
                setHistory = listOf(6 to 1),
                isUserServing = true,
            )
        paparazzi.snapshot("helpCircular") {
            WearTheme {
                WearScoreScreen(
                    scoreDisplay = score,
                    isConnected = true,
                    showHelp = true,
                    currentTime = "2:30 PM",
                    onNewMatch = {},
                    onUserScored = {},
                    onOpponentScored = {},
                    onUndo = {},
                )
            }
        }
    }
}
