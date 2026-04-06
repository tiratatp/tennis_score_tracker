package com.nuttyknot.tennisscoretracker.wear.screenshots

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.TimeSource
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

// Large font tests on Wear OS large round 1.39" (227dp) with fontScale=1.37
class WearScoreScreenLargeFontScreenshotTest {
    companion object {
        private val FIXED_TIME_SOURCE =
            object : TimeSource {
                @Composable
                override fun currentTime(): String = "2:30 PM"
            }

        // Wear OS large round 1.39" (227dp): 454x454 px, XHIGH density, round, large font
        private val WEAR_LARGE_ROUND_LARGE_FONT =
            DeviceConfig(
                screenWidth = 454,
                screenHeight = 454,
                density = Density.XHIGH,
                ratio = ScreenRatio.NOTLONG,
                size = ScreenSize.SMALL,
                screenRound = ScreenRound.ROUND,
                softButtons = false,
                fontScale = 1.37f,
            )
    }

    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = WEAR_LARGE_ROUND_LARGE_FONT,
            theme = "android:Theme.DeviceDefault",
            renderingMode = SessionParams.RenderingMode.NORMAL,
        )

    @Test
    fun watch() {
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
        paparazzi.snapshot("watchLargeFont") {
            WearTheme {
                WearScoreScreen(
                    scoreDisplay = score,
                    isConnected = true,
                    timeSource = FIXED_TIME_SOURCE,
                    onNewMatch = {},
                    onUserScored = {},
                    onOpponentScored = {},
                    onUndo = {},
                )
            }
        }
    }

    @Test
    fun matchOver() {
        val score =
            WearScoreDisplay(
                userName = "Sharapova",
                opponentName = "Williams",
                userScore = "0",
                opponentScore = "0",
                userGames = 6,
                opponentGames = 4,
                userSets = 2,
                opponentSets = 0,
                setHistory = listOf(6 to 1, 6 to 4),
                isUserServing = true,
                isMatchOver = true,
                matchWinner = "Sharapova",
            )
        paparazzi.snapshot("matchOverLargeFont") {
            WearTheme {
                WearScoreScreen(
                    scoreDisplay = score,
                    isConnected = true,
                    timeSource = FIXED_TIME_SOURCE,
                    onNewMatch = {},
                    onUserScored = {},
                    onOpponentScored = {},
                    onUndo = {},
                )
            }
        }
    }

    @Test
    fun watchLongNames() {
        val score =
            WearScoreDisplay(
                userName = "Sharapova-Williams",
                opponentName = "Alexandrova-Kuznetsova",
                userScore = "40",
                opponentScore = "30",
                userGames = 5,
                opponentGames = 4,
                userSets = 1,
                opponentSets = 0,
                setHistory = listOf(6 to 1),
                isUserServing = true,
            )
        paparazzi.snapshot("watchLongNamesLargeFont") {
            WearTheme {
                WearScoreScreen(
                    scoreDisplay = score,
                    isConnected = true,
                    timeSource = FIXED_TIME_SOURCE,
                    onNewMatch = {},
                    onUserScored = {},
                    onOpponentScored = {},
                    onUndo = {},
                )
            }
        }
    }

    @Test
    fun watchDisconnected() {
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
        paparazzi.snapshot("watchDisconnectedLargeFont") {
            WearTheme {
                WearScoreScreen(
                    scoreDisplay = score,
                    isConnected = false,
                    timeSource = FIXED_TIME_SOURCE,
                    onNewMatch = {},
                    onUserScored = {},
                    onOpponentScored = {},
                    onUndo = {},
                )
            }
        }
    }
}
