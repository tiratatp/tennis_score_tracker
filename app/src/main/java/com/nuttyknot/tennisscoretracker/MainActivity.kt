package com.nuttyknot.tennisscoretracker

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nuttyknot.tennisscoretracker.ui.TennisAppNavigation
import com.nuttyknot.tennisscoretracker.ui.theme.TennisScoreTrackerTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var scoreManager: ScoreManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var ttsManager: TtsManager
    private lateinit var keyEventManager: KeyEventManager
    private var pendingTheme: AppTheme? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep Screen On
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Enable Immersive Mode
        enableEdgeToEdge()
        hideSystemBars()

        initializeManagers()
        observeSettings()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                scoreManager.matchState.drop(1).collectLatest { state ->
                    state.announcement?.let { ttsManager.announce(it) }
                }
            }
        }

        setContent {
            val appTheme by settingsManager.appThemeFlow.collectAsState(
                initial = SettingsManager.DEFAULT_APP_THEME,
            )
            TennisScoreTrackerTheme(appTheme = appTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    TennisAppNavigation(
                        scoreManager = scoreManager,
                        settingsManager = settingsManager,
                    )
                }
            }
        }
    }

    private fun initializeManagers() {
        scoreManager = ScoreManager()
        settingsManager = SettingsManager(this)
        ttsManager = TtsManager(this)

        keyEventManager =
            KeyEventManager(
                scope = lifecycleScope,
                onSingleClick = { scoreManager.incrementUserScore() },
                onDoubleClick = { scoreManager.incrementOpponentScore() },
                onLongPress = { scoreManager.undo() },
            )
    }

    private fun observeSettings() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsManager.keycodeFlow.collectLatest { keycode ->
                    keyEventManager.targetKeyCode = keycode
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsManager.doubleClickLatencyFlow.collectLatest { latency ->
                    keyEventManager.doubleClickLatency = latency
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsManager.longPressLatencyFlow.collectLatest { latency ->
                    keyEventManager.longPressLatency = latency
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    settingsManager.userNameFlow,
                    settingsManager.opponentNameFlow,
                ) { user, opponent -> user to opponent }.collectLatest { (user, opponent) ->
                    scoreManager.updateMatchParameters(userName = user, opponentName = opponent)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsManager.initialServerIsUserFlow.collectLatest { isUser ->
                    scoreManager.updateMatchParameters(initialServerIsUser = isUser)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsManager.appThemeFlow.collectLatest { theme ->
                    pendingTheme = theme
                }
            }
        }
    }

    private fun updateLauncherIcon(activeTheme: AppTheme) {
        val pm = packageManager
        for (theme in AppTheme.entries) {
            val componentName = ComponentName(this, "$packageName${theme.aliasName}")
            val newState =
                if (theme == activeTheme) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
            if (pm.getComponentEnabledSetting(componentName) != newState) {
                pm.setComponentEnabledSetting(
                    componentName,
                    newState,
                    PackageManager.DONT_KILL_APP,
                )
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val handled =
            when (event.action) {
                KeyEvent.ACTION_DOWN -> keyEventManager.onKeyDown(event.keyCode, event)
                KeyEvent.ACTION_UP -> keyEventManager.onKeyUp(event.keyCode)
                else -> false
            }
        return if (handled) true else super.dispatchKeyEvent(event)
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onStop() {
        super.onStop()
        pendingTheme?.let { theme ->
            updateLauncherIcon(theme)
            pendingTheme = null
        }
    }

    override fun onDestroy() {
        ttsManager.shutdown()
        super.onDestroy()
    }
}
