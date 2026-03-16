package com.nuttyknot.tennisscoretracker

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.nuttyknot.tennisscoretracker.shared.WearConstants
import com.nuttyknot.tennisscoretracker.shared.WearScoreDisplay
import com.nuttyknot.tennisscoretracker.ui.theme.ColorblindBlue
import com.nuttyknot.tennisscoretracker.ui.theme.ColorblindOrange
import com.nuttyknot.tennisscoretracker.ui.theme.GrandSlamWhite
import com.nuttyknot.tennisscoretracker.ui.theme.GrandSlamYellow
import com.nuttyknot.tennisscoretracker.ui.theme.MiamiCyan
import com.nuttyknot.tennisscoretracker.ui.theme.MiamiMagenta
import com.nuttyknot.tennisscoretracker.ui.theme.SkyBlue
import com.nuttyknot.tennisscoretracker.ui.theme.White

private fun AppTheme.primaryArgb(): Int =
    when (this) {
        AppTheme.GRAND_SLAM -> GrandSlamYellow
        AppTheme.MIAMI_NIGHT -> MiamiCyan
        AppTheme.COLORBLIND_SAFE -> ColorblindOrange
        AppTheme.SKY_BLUE -> SkyBlue
    }.toArgb()

private fun AppTheme.secondaryArgb(): Int =
    when (this) {
        AppTheme.GRAND_SLAM -> GrandSlamWhite
        AppTheme.MIAMI_NIGHT -> MiamiMagenta
        AppTheme.COLORBLIND_SAFE -> ColorblindBlue
        AppTheme.SKY_BLUE -> White
    }.toArgb()

fun WearScoreDisplay.Companion.fromMatchState(
    state: TennisMatchState,
    appTheme: AppTheme,
): WearScoreDisplay =
    WearScoreDisplay(
        userName = state.userName,
        opponentName = state.opponentName,
        userScore = state.userScore.display,
        opponentScore = state.opponentScore.display,
        userGames = state.userGames,
        opponentGames = state.opponentGames,
        userSets = state.userSets,
        opponentSets = state.opponentSets,
        setHistory = state.setHistory,
        isUserServing = state.isUserServing,
        isMatchOver = state.matchWinner != null,
        matchWinner = state.matchWinner,
        primaryColorArgb = appTheme.primaryArgb(),
        secondaryColorArgb = appTheme.secondaryArgb(),
    )

class WearSyncManager(
    context: Context,
    private val onCommand: (String) -> Unit,
) : MessageClient.OnMessageReceivedListener {
    private val dataClient = Wearable.getDataClient(context)
    private val messageClient = Wearable.getMessageClient(context)

    fun start() {
        messageClient.addListener(this)
    }

    fun stop() {
        messageClient.removeListener(this)
    }

    fun pushState(
        state: TennisMatchState,
        appTheme: AppTheme,
    ) {
        val display = WearScoreDisplay.fromMatchState(state, appTheme)
        val putDataMapRequest =
            PutDataMapRequest.create(WearConstants.PATH_SCORE).apply {
                dataMap.putString(WearConstants.KEY_SCORE_JSON, display.toJson())
                dataMap.putLong(WearConstants.KEY_TIMESTAMP, System.currentTimeMillis())
            }
        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
        dataClient.putDataItem(putDataRequest)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == WearConstants.PATH_COMMAND) {
            val command = String(messageEvent.data)
            Log.d(TAG, "Received command from watch: $command")
            onCommand(command)
        }
    }

    companion object {
        private const val TAG = "WearSyncManager"
    }
}
