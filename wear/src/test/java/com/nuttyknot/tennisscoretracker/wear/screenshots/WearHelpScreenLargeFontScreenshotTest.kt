package com.nuttyknot.tennisscoretracker.wear.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.Density
import com.android.resources.ScreenRatio
import com.android.resources.ScreenRound
import com.android.resources.ScreenSize
import com.nuttyknot.tennisscoretracker.wear.ui.DEFAULT_PRIMARY_COLOR
import com.nuttyknot.tennisscoretracker.wear.ui.DEFAULT_SECONDARY_COLOR
import com.nuttyknot.tennisscoretracker.wear.ui.HelpDialogContent
import com.nuttyknot.tennisscoretracker.wear.ui.WearTheme
import org.junit.Rule
import org.junit.Test

// Large font test for help dialog on Wear OS large round 1.39" (227dp) with fontScale=1.37
class WearHelpScreenLargeFontScreenshotTest {
    companion object {
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
    fun help() {
        paparazzi.snapshot("helpLargeFont") {
            WearTheme {
                HelpDialogContent(
                    userColor = DEFAULT_PRIMARY_COLOR,
                    opponentColor = DEFAULT_SECONDARY_COLOR,
                )
            }
        }
    }
}
