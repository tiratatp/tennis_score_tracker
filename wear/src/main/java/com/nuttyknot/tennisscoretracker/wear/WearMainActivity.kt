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
import androidx.wear.ambient.AmbientLifecycleObserver
import com.nuttyknot.tennisscoretracker.shared.WearConstants
import com.nuttyknot.tennisscoretracker.wear.ui.WearScoreScreen
import com.nuttyknot.tennisscoretracker.wear.ui.WearTheme

class WearMainActivity : ComponentActivity() {
    private val viewModel: WearRemoteViewModel by viewModels()
    private var isAmbient by mutableStateOf(false)
    private var showHelp by mutableStateOf(false)

    private val ambientCallback =
        object : AmbientLifecycleObserver.AmbientLifecycleCallback {
            override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
                isAmbient = true
                viewModel.onAmbientStateChanged(true)
            }

            override fun onExitAmbient() {
                isAmbient = false
                viewModel.onAmbientStateChanged(false)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(AmbientLifecycleObserver(this, ambientCallback))
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
                    showHelp = showHelp,
                    onDismissHelp = {
                        showHelp = false
                        prefs.edit().putBoolean(KEY_HAS_SEEN_HELP, true).apply()
                    },
                    onUserScored = { viewModel.sendCommand(WearConstants.CMD_USER_SCORED) },
                    onOpponentScored = { viewModel.sendCommand(WearConstants.CMD_OPPONENT_SCORED) },
                    onUndo = { viewModel.sendCommand(WearConstants.CMD_UNDO) },
                )
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "wear_tennis_prefs"
        private const val KEY_HAS_SEEN_HELP = "has_seen_help"
    }
}
