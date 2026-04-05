package com.nuttyknot.tennisscoretracker.wear

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.nuttyknot.tennisscoretracker.shared.WearConstants
import com.nuttyknot.tennisscoretracker.shared.WearScoreDisplay
import com.nuttyknot.tennisscoretracker.wear.ui.WearScoreScreen
import com.nuttyknot.tennisscoretracker.wear.ui.WearTheme
import kotlinx.coroutines.launch

class WearMainActivity : ComponentActivity() {
    private val viewModel: WearRemoteViewModel by viewModels()
    private var isAmbient by mutableStateOf(false)
    private var showHelp by mutableStateOf(false)
    private var burnInProtectionRequired by mutableStateOf(false)
    private var burnInOffset by mutableStateOf(IntOffset.Zero)
    private var lowBitAmbient by mutableStateOf(false)
    private var isOngoingActivityActive = false

    private val ambientCallback =
        object : AmbientLifecycleObserver.AmbientLifecycleCallback {
            override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
                isAmbient = true
                burnInProtectionRequired = ambientDetails.burnInProtectionRequired
                lowBitAmbient = ambientDetails.deviceHasLowBitAmbient
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
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }
        lifecycleScope.launch {
            viewModel.scoreDisplay.collect { score ->
                if (!score.isMatchOver) {
                    updateOngoingActivity(score)
                } else {
                    stopOngoingActivity()
                }
            }
        }

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
                    lowBitAmbient = lowBitAmbient,
                    ambientOffset = burnInOffset,
                    showHelp = showHelp,
                    onDismissHelp = {
                        showHelp = false
                        prefs.edit().putBoolean(KEY_HAS_SEEN_HELP, true).apply()
                    },
                    onShowHelp = { showHelp = true },
                    onNewMatch = { viewModel.sendCommand(WearConstants.CMD_RESET) },
                    onEndMatch = { viewModel.sendCommand(WearConstants.CMD_END_MATCH) },
                    onUserScored = { viewModel.sendCommand(WearConstants.CMD_USER_SCORED) },
                    onOpponentScored = { viewModel.sendCommand(WearConstants.CMD_OPPONENT_SCORED) },
                    onUndo = { viewModel.sendCommand(WearConstants.CMD_UNDO) },
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopOngoingActivity()
        lifecycle.removeObserver(ambientObserver)
    }

    @SuppressLint("MissingPermission")
    private fun updateOngoingActivity(score: WearScoreDisplay) {
        val nm = NotificationManagerCompat.from(this)
        if (!nm.areNotificationsEnabled()) return

        val intent =
            Intent(this, WearMainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val statusText = "${score.userScore} - ${score.opponentScore}"
        val notificationBuilder =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .setSilent(true)

        val ongoingActivity =
            OngoingActivity
                .Builder(this, NOTIFICATION_ID, notificationBuilder)
                .setStaticIcon(R.drawable.ic_ongoing)
                .setTouchIntent(pendingIntent)
                .setStatus(
                    Status
                        .Builder()
                        .addTemplate(statusText)
                        .build(),
                ).build()

        ongoingActivity.apply(this)
        nm.notify(NOTIFICATION_ID, notificationBuilder.build())
        isOngoingActivityActive = true
    }

    private fun stopOngoingActivity() {
        if (isOngoingActivityActive) {
            NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
            isOngoingActivityActive = false
        }
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "Match in Progress",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                setShowBadge(false)
            }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val PREFS_NAME = "wear_tennis_prefs"
        private const val KEY_HAS_SEEN_HELP = "has_seen_help"
        private const val BURN_IN_OFFSET_RANGE = 10
        private const val CHANNEL_ID = "match_ongoing"
        private const val NOTIFICATION_ID = 1
    }
}
