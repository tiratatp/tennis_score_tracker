package com.nuttyknot.tennisscoretracker

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuttyknot.tennisscoretracker.announcements.generateAnnouncement
import com.nuttyknot.tennisscoretracker.scoring.FormatConfig
import com.nuttyknot.tennisscoretracker.scoring.RallyScoring
import com.nuttyknot.tennisscoretracker.scoring.TennisScoring
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ScoreModel(
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    private val settingsManager: SettingsManager? = null,
) : ViewModel() {
    private val _matchState =
        MutableStateFlow(
            restoreState(),
        )
    val matchState: StateFlow<MatchState> = _matchState.asStateFlow()

    private val historyStack = ArrayDeque<MatchState>()
    private val processor = Processor()

    @Suppress("TooGenericExceptionCaught")
    private fun restoreState(): MatchState {
        settingsManager?.let { sm ->
            try {
                val persisted = runBlocking { sm.matchStateFlow.first() }
                if (persisted != null) return persisted
            } catch (_: Exception) {
                // Fall through to SavedStateHandle
            }
        }
        return savedStateHandle.get<Bundle>(SAVED_STATE_KEY)?.let {
            matchStateFromBundle(it)
        } ?: MatchState()
    }

    private data class MatchConfig(
        val userName: String = "",
        val opponentName: String = "",
        val initialServerIsUser: Boolean = true,
        val matchFormat: MatchFormat = MatchFormat.STANDARD,
    )

    private var config = MatchConfig()

    init {
        _matchState.value.let { restored ->
            if (!restored.isScoreZero) {
                config =
                    MatchConfig(
                        userName = restored.userName,
                        opponentName = restored.opponentName,
                        matchFormat = restored.matchFormat,
                    )
            }
        }
    }

    private fun saveState() {
        savedStateHandle[SAVED_STATE_KEY] = _matchState.value.toBundle()
        settingsManager?.let { sm ->
            viewModelScope.launch { sm.saveMatchState(_matchState.value) }
        }
    }

    fun updateMatchParameters(
        userName: String? = null,
        opponentName: String? = null,
        initialServerIsUser: Boolean? = null,
        matchFormat: MatchFormat? = null,
    ) {
        config =
            config.copy(
                userName = userName ?: config.userName,
                opponentName = opponentName ?: config.opponentName,
                initialServerIsUser = initialServerIsUser ?: config.initialServerIsUser,
                matchFormat = matchFormat ?: config.matchFormat,
            )

        _matchState.update { currentState ->
            val canUpdateServer = currentState.isScoreZero && initialServerIsUser != null
            val canUpdateFormat = currentState.isScoreZero && matchFormat != null
            val newScore = if (canUpdateFormat) initialScoreForFormat(matchFormat!!) else null
            currentState.copy(
                userName = userName ?: currentState.userName,
                opponentName = opponentName ?: currentState.opponentName,
                isUserServing = if (canUpdateServer) initialServerIsUser!! else currentState.isUserServing,
                userScore = newScore ?: currentState.userScore,
                opponentScore = newScore ?: currentState.opponentScore,
                matchFormat = if (canUpdateFormat) matchFormat!! else currentState.matchFormat,
                announcement = null,
            )
        }
    }

    private fun initialScoreForFormat(format: MatchFormat): PlayerScore =
        if (format.sport != Sport.TENNIS) PlayerScore.TiebreakScore(0) else PlayerScore.Love

    fun incrementUserScore() = scorePoint(userScored = true)

    fun incrementOpponentScore() = scorePoint(userScored = false)

    private fun scorePoint(userScored: Boolean) {
        if (_matchState.value.matchWinner != null) return
        historyStack.addLast(_matchState.value)
        _matchState.update { processor.calculateNextState(it, userScored) }
        saveState()
    }

    fun undo() {
        if (historyStack.isNotEmpty()) {
            val previousState = historyStack.removeLast()
            _matchState.value = previousState.copy(announcement = null)
            saveState()
        }
    }

    fun reset() {
        historyStack.clear()
        val fc = FormatConfig.forFormat(config.matchFormat)
        val initialScore =
            if (fc.isRallyScoring || fc.isSideOutScoring) {
                PlayerScore.TiebreakScore(0)
            } else {
                PlayerScore.Love
            }
        _matchState.value =
            MatchState(
                userScore = initialScore,
                opponentScore = initialScore,
                userName = config.userName,
                opponentName = config.opponentName,
                isUserServing = config.initialServerIsUser,
                matchFormat = config.matchFormat,
                announcement = null,
            )
        saveState()
    }

    fun startNextSet() {
        _matchState.update { currentState ->
            if (currentState.setWinner != null) {
                processor.prepareNextSet(currentState)
            } else {
                currentState
            }
        }
        saveState()
    }

    private inner class Processor {
        fun calculateNextState(
            currentState: MatchState,
            userScored: Boolean,
        ): MatchState {
            if (currentState.setWinner != null) return prepareNextSet(currentState)

            val baseState =
                currentState.copy(
                    gameWinner = null,
                    setWinner = null,
                    matchWinner = null,
                    isNewSet = false,
                    announcement = null,
                )

            return calculateScore(baseState, userScored)
        }

        private fun calculateScore(
            baseState: MatchState,
            userScored: Boolean,
        ): MatchState {
            val fc = FormatConfig.forFormat(config.matchFormat)

            return when {
                fc.isRallyScoring ->
                    RallyScoring.calculateRallyScore(baseState, userScored, fc, config.matchFormat.sport)
                fc.isSideOutScoring ->
                    RallyScoring.calculateSideOutScore(baseState, userScored, fc)
                baseState.isMatchTiebreak ->
                    TennisScoring.handleTiebreakScoring(baseState, userScored, fc.matchTiebreakPoints, fc)
                else -> TennisScoring.calculateScore(baseState, userScored, fc)
            }
        }

        fun prepareNextSet(currentState: MatchState): MatchState {
            val fc = FormatConfig.forFormat(config.matchFormat)
            val isMatchTiebreak =
                fc.useMatchTiebreak &&
                    currentState.userSets == 1 && currentState.opponentSets == 1
            val initialScore =
                if (fc.isRallyScoring || fc.isSideOutScoring || isMatchTiebreak) {
                    PlayerScore.TiebreakScore(0)
                } else {
                    PlayerScore.Love
                }

            return currentState.copy(
                userScore = initialScore,
                opponentScore = initialScore,
                userGames = 0,
                opponentGames = 0,
                setWinner = null,
                gameWinner = null,
                isNewSet = true,
                isMatchTiebreak = isMatchTiebreak,
            ).let { it.copy(announcement = generateAnnouncement(it)) }
        }
    }

    companion object {
        private const val SAVED_STATE_KEY = "tennis_match_state"
    }
}
