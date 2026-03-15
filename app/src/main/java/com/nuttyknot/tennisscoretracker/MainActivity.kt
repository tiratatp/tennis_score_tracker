package com.nuttyknot.tennisscoretracker

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.nuttyknot.tennisscoretracker.ui.TennisAppNavigation
import com.nuttyknot.tennisscoretracker.ui.theme.TennisScoreTrackerTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var scoreManager: ScoreManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var ttsManager: TtsManager
    private lateinit var keyEventManager: KeyEventManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Keep Screen On
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Enable Immersive Mode
        enableEdgeToEdge()
        hideSystemBars()

        scoreManager = ScoreManager()
        settingsManager = SettingsManager(this)
        ttsManager = TtsManager(this)

        keyEventManager = KeyEventManager(
            scope = lifecycleScope,
            onSingleClick = { scoreManager.incrementUserScore() },
            onDoubleClick = { scoreManager.incrementOpponentScore() },
            onLongPress = { scoreManager.undo() }
        )

        lifecycleScope.launch {
            settingsManager.keycodeFlow.collectLatest { keycode ->
                keyEventManager.targetKeyCode = keycode
            }
        }
        lifecycleScope.launch {
            settingsManager.doubleClickLatencyFlow.collectLatest { latency ->
                keyEventManager.doubleClickLatency = latency
            }
        }
        lifecycleScope.launch {
            settingsManager.longPressLatencyFlow.collectLatest { latency ->
                keyEventManager.longPressLatency = latency
            }
        }
        lifecycleScope.launch {
            kotlinx.coroutines.flow.combine(
                settingsManager.userNameFlow,
                settingsManager.opponentNameFlow
            ) { user, opponent -> user to opponent }.collectLatest { (user, opponent) ->
                scoreManager.updateNames(user, opponent)
            }
        }
        lifecycleScope.launch {
            settingsManager.initialServerIsUserFlow.collectLatest { isUser ->
                scoreManager.updateInitialServer(isUser)
            }
        }

        lifecycleScope.launch {
            scoreManager.matchState.drop(1).collectLatest { state ->
                state.announcement?.let { ttsManager.announce(it) }
            }
        }

        setContent {
            TennisScoreTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TennisAppNavigation(
                        scoreManager = scoreManager,
                        settingsManager = settingsManager
                    )
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyEventManager.onKeyDown(event.keyCode, event)) {
                return true
            }
        } else if (event.action == KeyEvent.ACTION_UP) {
            if (keyEventManager.onKeyUp(event.keyCode)) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
