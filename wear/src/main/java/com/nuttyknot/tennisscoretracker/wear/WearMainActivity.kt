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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.wear.ambient.AmbientLifecycleObserver
import com.nuttyknot.tennisscoretracker.shared.WearConstants
import com.nuttyknot.tennisscoretracker.wear.ui.WearScoreScreen
import com.nuttyknot.tennisscoretracker.wear.ui.WearTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class WearMainActivity : ComponentActivity() {
    private val viewModel: WearRemoteViewModel by viewModels()
    private var isAmbient by mutableStateOf(false)
    private var showHelp by mutableStateOf(false)
    private var currentTime by mutableStateOf("")
    private var timeUpdateJob: Job? = null

    private val ambientCallback =
        object : AmbientLifecycleObserver.AmbientLifecycleCallback {
            override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
                isAmbient = true
                viewModel.onAmbientStateChanged(true)
                timeUpdateJob?.cancel()
                updateCurrentTime()
            }

            override fun onUpdateAmbient() {
                updateCurrentTime()
            }

            override fun onExitAmbient() {
                isAmbient = false
                viewModel.onAmbientStateChanged(false)
                startTimeUpdates()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(AmbientLifecycleObserver(this, ambientCallback))
        viewModel.startListening()
        startTimeUpdates()

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
                    currentTime = currentTime,
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

    private fun updateCurrentTime() {
        val format = android.text.format.DateFormat.getTimeFormat(this)
        currentTime = format.format(Date())
    }

    private fun startTimeUpdates() {
        timeUpdateJob?.cancel()
        updateCurrentTime()
        timeUpdateJob =
            lifecycleScope.launch {
                while (true) {
                    val now = Calendar.getInstance()
                    val delayMs =
                        (SECONDS_PER_MINUTE - now.get(Calendar.SECOND)) * MILLIS_PER_SECOND.toLong() -
                            now.get(Calendar.MILLISECOND)
                    delay(delayMs)
                    updateCurrentTime()
                }
            }
    }

    companion object {
        private const val PREFS_NAME = "wear_tennis_prefs"
        private const val KEY_HAS_SEEN_HELP = "has_seen_help"
        private const val SECONDS_PER_MINUTE = 60
        private const val MILLIS_PER_SECOND = 1000
    }
}
