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
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nuttyknot.tennisscoretracker.shared.WearConstants
import com.nuttyknot.tennisscoretracker.ui.AppNavigation
import com.nuttyknot.tennisscoretracker.ui.Routes
import com.nuttyknot.tennisscoretracker.ui.SystemBarsEffect
import com.nuttyknot.tennisscoretracker.ui.theme.TennisScoreTrackerTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val settingsManager by lazy { SettingsManager(this) }
    private val scoreModel: ScoreModel by viewModels {
        object : AbstractSavedStateViewModelFactory(this, null) {
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle,
            ): T {
                @Suppress("UNCHECKED_CAST")
                return ScoreModel(handle, settingsManager) as T
            }
        }
    }
    private lateinit var ttsManager: TtsManager
    private lateinit var keyEventManager: KeyEventManager
    private lateinit var wearSyncManager: WearSyncManager
    private var pendingTheme: AppTheme? = null
    private var isOnScoreScreen: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep Screen On
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()

        initializeManagers()
        wearSyncManager.start()
        observeSettings()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                combine(
                    scoreModel.matchState.drop(1),
                    settingsManager.ttsEnabledFlow.distinctUntilChanged(),
                ) { state, enabled -> state to enabled }
                    .collectLatest { (state, enabled) ->
                        if (enabled) {
                            state.announcement?.let { ttsManager.announce(it) }
                        }
                    }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                combine(
                    scoreModel.matchState,
                    settingsManager.appThemeFlow,
                ) { state, theme -> state to theme }
                    .collectLatest { (state, theme) ->
                        wearSyncManager.pushState(state, theme)
                    }
            }
        }

        setContent {
            val appTheme by settingsManager.appThemeFlow.collectAsState(
                initial = SettingsManager.DEFAULT_APP_THEME,
            )
            TennisScoreTrackerTheme(appTheme = appTheme) {
                SystemBarsEffect()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation(
                        scoreModel = scoreModel,
                        settingsManager = settingsManager,
                        availableVoices = ttsManager.availableVoices,
                        onRouteChange = { route ->
                            isOnScoreScreen = route == Routes.SCORE_SCREEN
                        },
                    )
                }
            }
        }
    }

    private fun initializeManagers() {
        ttsManager = TtsManager(this)

        keyEventManager =
            KeyEventManager(
                scope = lifecycleScope,
                onSingleClick = { scoreModel.incrementUserScore() },
                onDoubleClick = { scoreModel.incrementOpponentScore() },
                onLongPress = { scoreModel.undo() },
            )

        wearSyncManager =
            WearSyncManager(this) { command ->
                when (command) {
                    WearConstants.CMD_USER_SCORED -> scoreModel.incrementUserScore()
                    WearConstants.CMD_OPPONENT_SCORED -> scoreModel.incrementOpponentScore()
                    WearConstants.CMD_UNDO -> scoreModel.undo()
                    WearConstants.CMD_RESET -> scoreModel.reset()
                }
            }
    }

    @Suppress("LongMethod")
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
                    scoreModel.updateMatchParameters(userName = user, opponentName = opponent)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsManager.initialServerIsUserFlow.collectLatest { isUser ->
                    scoreModel.updateMatchParameters(initialServerIsUser = isUser)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsManager.matchFormatFlow.collectLatest { format ->
                    scoreModel.updateMatchParameters(matchFormat = format)
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                var isFirst = true
                settingsManager.announcerVoiceFlow
                    .distinctUntilChanged()
                    .collectLatest { voiceName ->
                        ttsManager.setVoice(voiceName)
                        if (!isFirst) {
                            val preview = scoreModel.matchState.value.announcement ?: VOICE_PREVIEW
                            ttsManager.announce(preview)
                        }
                        isFirst = false
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
            when {
                settingsManager.isDetectingKeycode.value -> {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        settingsManager.onKeycodeDetected(event.keyCode)
                    }
                    true
                }
                isOnScoreScreen ->
                    when (event.action) {
                        KeyEvent.ACTION_DOWN -> keyEventManager.onKeyDown(event.keyCode, event)
                        KeyEvent.ACTION_UP -> keyEventManager.onKeyUp(event.keyCode)
                        else -> false
                    }
                else -> false
            }
        return if (handled) true else super.dispatchKeyEvent(event)
    }

    override fun onStop() {
        super.onStop()
        pendingTheme?.let { theme ->
            updateLauncherIcon(theme)
            pendingTheme = null
        }
    }

    override fun onDestroy() {
        wearSyncManager.stop()
        ttsManager.shutdown()
        super.onDestroy()
    }

    private companion object {
        const val VOICE_PREVIEW = "15 Love"
    }
}
