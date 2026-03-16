package com.nuttyknot.tennisscoretracker

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.nuttyknot.tennisscoretracker.shared.WearConstants
import com.nuttyknot.tennisscoretracker.shared.WearScoreDisplay

fun WearScoreDisplay.Companion.fromMatchState(state: TennisMatchState): WearScoreDisplay =
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

    fun pushState(state: TennisMatchState) {
        val display = WearScoreDisplay.fromMatchState(state)
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
