package com.nuttyknot.tennisscoretracker.wear

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.ambient.AmbientLifecycleObserver
import com.nuttyknot.tennisscoretracker.shared.WearConstants
import com.nuttyknot.tennisscoretracker.wear.ui.WearScoreScreen
import com.nuttyknot.tennisscoretracker.wear.ui.WearTheme

class WearMainActivity : ComponentActivity() {
    private val viewModel: WearRemoteViewModel by viewModels()
    private var isAmbient by mutableStateOf(false)
    private var showHelp by mutableStateOf(false)
    private var burnInProtectionRequired by mutableStateOf(false)
    private var burnInOffset by mutableStateOf(IntOffset.Zero)

    private val ambientCallback =
        object : AmbientLifecycleObserver.AmbientLifecycleCallback {
            override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
                isAmbient = true
                burnInProtectionRequired = ambientDetails.burnInProtectionRequired
                viewModel.onAmbientStateChanged(true)
            }

            override fun onUpdateAmbient() {
                if (burnInProtectionRequired) {
                    burnInOffset =
                        IntOffset(
                            (-BURN_IN_OFFSET_RANGE..BURN_IN_OFFSET_RANGE).random(),
                            (-BURN_IN_OFFSET_RANGE..BURN_IN_OFFSET_RANGE).random(),
                        )
                }
            }

            override fun onExitAmbient() {
                isAmbient = false
                burnInOffset = IntOffset.Zero
                viewModel.onAmbientStateChanged(false)
            }
        }

    private val ambientObserver = AmbientLifecycleObserver(this, ambientCallback)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(ambientObserver)
        viewModel.startListening()

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        showHelp = !prefs.getBoolean(KEY_HAS_SEEN_HELP, false)

        setContent {
            val scoreDisplay by viewModel.scoreDisplay.collectAsState()
            val isConnected by viewModel.isConnected.collectAsState()

            WearTheme {
                WearScoreScreen(
                    scoreDisplay = scoreDisplay,
                    isConnected = isConnected,
                    isAmbient = isAmbient,
                    burnInProtectionRequired = burnInProtectionRequired,
                    ambientOffset = burnInOffset,
                    showHelp = showHelp,
                    onDismissHelp = {
                        showHelp = false
                        prefs.edit().putBoolean(KEY_HAS_SEEN_HELP, true).apply()
                    },
                    onShowHelp = { showHelp = true },
                    onNewMatch = { viewModel.sendCommand(WearConstants.CMD_RESET) },
                    onUserScored = { viewModel.sendCommand(WearConstants.CMD_USER_SCORED) },
                    onOpponentScored = { viewModel.sendCommand(WearConstants.CMD_OPPONENT_SCORED) },
                    onUndo = { viewModel.sendCommand(WearConstants.CMD_UNDO) },
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(ambientObserver)
    }

    companion object {
        private const val PREFS_NAME = "wear_tennis_prefs"
        private const val KEY_HAS_SEEN_HELP = "has_seen_help"
        private const val BURN_IN_OFFSET_RANGE = 10
    }
}
