package com.nuttyknot.tennisscoretracker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private object ScoringConstants {
    const val GAMES_TO_WIN_SET = 6
    const val GAMES_TO_WIN_SET_LONG = 7
    const val GAME_DIFFERENCE_FOR_SET = 2
    const val SETS_TO_WIN_MATCH = 2
    const val TIEBREAK_WIN_THRESHOLD = 7
    const val TIEBREAK_SERVER_CYCLE = 4
    const val TIEBREAK_CYCLE_SAME_END = 3
}

class ScoreManager {
    private val _matchState = MutableStateFlow(TennisMatchState())
    val matchState: StateFlow<TennisMatchState> = _matchState.asStateFlow()

    private val historyStack = ArrayDeque<TennisMatchState>()
    private val processor = Processor()

    private data class MatchConfig(
        val userName: String = "",
        val opponentName: String = "",
        val initialServerIsUser: Boolean = true,
    )

    private var config = MatchConfig()

    fun updateMatchParameters(
        userName: String? = null,
        opponentName: String? = null,
        initialServerIsUser: Boolean? = null,
    ) {
        config =
            config.copy(
                userName = userName ?: config.userName,
                opponentName = opponentName ?: config.opponentName,
                initialServerIsUser = initialServerIsUser ?: config.initialServerIsUser,
            )

        _matchState.update { currentState ->
            val canUpdateServer = canUpdateInitialServer() && initialServerIsUser != null
            currentState.copy(
                userName = userName ?: currentState.userName,
                opponentName = opponentName ?: currentState.opponentName,
                isUserServing = if (canUpdateServer) initialServerIsUser!! else currentState.isUserServing,
                gameWinner = null,
                setWinner = null,
                matchWinner = null,
                isNewSet = false,
                announcement = null,
            )
        }
    }

    private fun canUpdateInitialServer(): Boolean {
        val state = _matchState.value
        return historyStack.isEmpty() &&
            state.userGames == 0 &&
            state.opponentGames == 0 &&
            state.userScore == PlayerScore.Love &&
            state.opponentScore == PlayerScore.Love
    }

    fun incrementUserScore() = scorePoint(userScored = true)

    fun incrementOpponentScore() = scorePoint(userScored = false)

    private fun scorePoint(userScored: Boolean) {
        if (_matchState.value.matchWinner != null) return
        historyStack.addLast(_matchState.value)
        _matchState.update { processor.calculateNextState(it, userScored) }
    }

    fun undo() {
        if (historyStack.isNotEmpty()) {
            val previousState = historyStack.removeLast()
            _matchState.value = previousState.copy(announcement = null)
        }
    }

    fun reset() {
        historyStack.clear()
        _matchState.value =
            TennisMatchState(
                userName = config.userName,
                opponentName = config.opponentName,
                isUserServing = config.initialServerIsUser,
                announcement = null,
            )
    }

    fun startNextSet() {
        _matchState.update { currentState ->
            if (currentState.setWinner != null) {
                processor.prepareNextSet(currentState)
            } else {
                currentState
            }
        }
    }

    private inner class Processor {
        fun calculateNextState(
            currentState: TennisMatchState,
            userScored: Boolean,
        ): TennisMatchState {
            if (currentState.setWinner != null) {
                return prepareNextSet(currentState)
            }

            val baseState =
                currentState.copy(
                    gameWinner = null,
                    setWinner = null,
                    matchWinner = null,
                    isNewSet = false,
                    announcement = null,
                )

            val scoringPlayerScore = if (userScored) baseState.userScore else baseState.opponentScore
            val otherPlayerScore = if (userScored) baseState.opponentScore else baseState.userScore

            return when {
                baseState.userGames == ScoringConstants.GAMES_TO_WIN_SET &&
                    baseState.opponentGames == ScoringConstants.GAMES_TO_WIN_SET ->
                    handleTiebreakScoring(baseState, userScored)
                baseState.isDeuce -> ScoringLogic.handleDeuceScoring(baseState, userScored)
                scoringPlayerScore is PlayerScore.Advantage -> winGame(baseState, userScored)
                otherPlayerScore is PlayerScore.Advantage -> ScoringLogic.handleBackToDeuce(baseState)
                scoringPlayerScore == PlayerScore.Forty -> winGame(baseState, userScored)
                else -> ScoringLogic.handleRegularScoring(baseState, userScored, scoringPlayerScore)
            }
        }

        fun prepareNextSet(currentState: TennisMatchState) =
            currentState.copy(
                userGames = 0,
                opponentGames = 0,
                setWinner = null,
                gameWinner = null,
                isNewSet = true,
            ).let { it.copy(announcement = generateAnnouncement(it)) }

        private fun handleTiebreakScoring(
            state: TennisMatchState,
            userScored: Boolean,
        ): TennisMatchState {
            val userCurrent = (state.userScore as? PlayerScore.TiebreakScore)?.points ?: 0
            val oppCurrent = (state.opponentScore as? PlayerScore.TiebreakScore)?.points ?: 0

            val newUserPoints = if (userScored) userCurrent + 1 else userCurrent
            val newOppPoints = if (userScored) oppCurrent else oppCurrent + 1

            val isTiebreakWon =
                (
                    newUserPoints >= ScoringConstants.TIEBREAK_WIN_THRESHOLD ||
                        newOppPoints >= ScoringConstants.TIEBREAK_WIN_THRESHOLD
                ) &&
                    kotlin.math.abs(newUserPoints - newOppPoints) >= ScoringConstants.GAME_DIFFERENCE_FOR_SET

            val totalOldPoints = userCurrent + oppCurrent

            return if (isTiebreakWon) {
                val winnerName =
                    if (newUserPoints > newOppPoints) {
                        state.userName.ifEmpty { "User" }
                    } else {
                        state.opponentName.ifEmpty { "Opponent" }
                    }

                handleTiebreakWin(
                    state,
                    newUserPoints,
                    newOppPoints,
                    totalOldPoints,
                    winnerName,
                )
            } else {
                handleTiebreakContinue(
                    state,
                    newUserPoints,
                    newOppPoints,
                    totalOldPoints,
                )
            }
        }

        private fun winGame(
            currentState: TennisMatchState,
            userScored: Boolean,
        ): TennisMatchState {
            val userGamesForCheck = if (userScored) currentState.userGames + 1 else currentState.userGames
            val opponentGamesForCheck = if (userScored) currentState.opponentGames else currentState.opponentGames + 1
            val winnerName =
                if (userScored) {
                    currentState.userName.ifEmpty { "User" }
                } else {
                    currentState.opponentName.ifEmpty { "Opponent" }
                }

            return if (isSetWon(userGamesForCheck, opponentGamesForCheck)) {
                handleSetWin(
                    currentState,
                    userGamesForCheck,
                    opponentGamesForCheck,
                    userScored,
                    winnerName,
                )
            } else {
                val isNextTiebreak =
                    userGamesForCheck == ScoringConstants.GAMES_TO_WIN_SET &&
                        opponentGamesForCheck == ScoringConstants.GAMES_TO_WIN_SET
                val initialScore = if (isNextTiebreak) PlayerScore.TiebreakScore(0) else PlayerScore.Love

                val gameWonState =
                    currentState.copy(
                        userScore = initialScore,
                        opponentScore = initialScore,
                        userGames = userGamesForCheck,
                        opponentGames = opponentGamesForCheck,
                        isUserServing = !currentState.isUserServing,
                        gameWinner = winnerName,
                    )
                gameWonState.copy(announcement = generateAnnouncement(gameWonState))
            }
        }

        private fun isSetWon(
            winnerGames: Int,
            loserGames: Int,
        ) = (
            winnerGames >= ScoringConstants.GAMES_TO_WIN_SET &&
                winnerGames - loserGames >= ScoringConstants.GAME_DIFFERENCE_FOR_SET
        ) ||
            (winnerGames == ScoringConstants.GAMES_TO_WIN_SET_LONG)

        private fun handleSetWin(
            currentState: TennisMatchState,
            userGames: Int,
            opponentGames: Int,
            isUserWinner: Boolean,
            winnerName: String,
        ): TennisMatchState {
            val newSetHistory = currentState.setHistory + (userGames to opponentGames)
            val newUserSets = if (isUserWinner) currentState.userSets + 1 else currentState.userSets
            val newOpponentSets = if (isUserWinner) currentState.opponentSets else currentState.opponentSets + 1

            val isMatchWon =
                newUserSets >= ScoringConstants.SETS_TO_WIN_MATCH ||
                    newOpponentSets >= ScoringConstants.SETS_TO_WIN_MATCH

            val nextState =
                currentState.copy(
                    userScore = PlayerScore.Love,
                    opponentScore = PlayerScore.Love,
                    userGames = userGames,
                    opponentGames = opponentGames,
                    userSets = newUserSets,
                    opponentSets = newOpponentSets,
                    setHistory = newSetHistory,
                    matchWinner = if (isMatchWon) winnerName else null,
                    setWinner = if (!isMatchWon) winnerName else null,
                    isNewSet = !isMatchWon,
                    isUserServing = !currentState.isUserServing,
                )
            return nextState.copy(announcement = generateAnnouncement(nextState))
        }

        /**
         * Recovers the initial tiebreak server from the current server and point count.
         * The tiebreak server pattern repeats every 4 points: same, flipped, flipped, same.
         */
        private fun initialTiebreakServer(
            currentServer: Boolean,
            currentPointIndex: Int,
        ): Boolean =
            when (currentPointIndex % ScoringConstants.TIEBREAK_SERVER_CYCLE) {
                0, ScoringConstants.TIEBREAK_CYCLE_SAME_END -> currentServer
                else -> !currentServer
            }

        private fun handleTiebreakWin(
            state: TennisMatchState,
            newUserPoints: Int,
            newOppPoints: Int,
            totalOldPoints: Int,
            winnerName: String,
        ): TennisMatchState {
            val userGamesFinal =
                if (newUserPoints > newOppPoints) {
                    ScoringConstants.GAMES_TO_WIN_SET_LONG
                } else {
                    ScoringConstants.GAMES_TO_WIN_SET
                }
            val oppGamesFinal =
                if (newUserPoints > newOppPoints) {
                    ScoringConstants.GAMES_TO_WIN_SET
                } else {
                    ScoringConstants.GAMES_TO_WIN_SET_LONG
                }

            // Recover who served the first point; handleSetWin flips for next set
            val tiebreakFirstServer = initialTiebreakServer(state.isUserServing, totalOldPoints)

            return handleSetWin(
                state.copy(isUserServing = tiebreakFirstServer),
                userGamesFinal,
                oppGamesFinal,
                newUserPoints > newOppPoints,
                winnerName,
            )
        }

        private fun handleTiebreakContinue(
            state: TennisMatchState,
            newUserPoints: Int,
            newOppPoints: Int,
            totalOldPoints: Int,
        ): TennisMatchState {
            // Server switches when totalOldPoints is even (after 1st point, then every 2 points)
            val nextState =
                state.copy(
                    userScore = PlayerScore.TiebreakScore(newUserPoints),
                    opponentScore = PlayerScore.TiebreakScore(newOppPoints),
                    isUserServing =
                        if (totalOldPoints % 2 == 0) !state.isUserServing else state.isUserServing,
                )
            return nextState.copy(announcement = generateAnnouncement(nextState))
        }
    }

    private object ScoringLogic {
        fun handleDeuceScoring(
            state: TennisMatchState,
            userScored: Boolean,
        ): TennisMatchState {
            val nextState =
                if (userScored) {
                    state.copy(userScore = PlayerScore.Advantage)
                } else {
                    state.copy(opponentScore = PlayerScore.Advantage)
                }
            return nextState.copy(announcement = generateAnnouncement(nextState))
        }

        fun handleBackToDeuce(state: TennisMatchState): TennisMatchState {
            val nextState =
                state.copy(
                    userScore = PlayerScore.Forty,
                    opponentScore = PlayerScore.Forty,
                )
            return nextState.copy(announcement = generateAnnouncement(nextState))
        }

        fun handleRegularScoring(
            state: TennisMatchState,
            userScored: Boolean,
            scoringPlayerScore: PlayerScore,
        ): TennisMatchState {
            val nextScore =
                when (scoringPlayerScore) {
                    PlayerScore.Love -> PlayerScore.Fifteen
                    PlayerScore.Fifteen -> PlayerScore.Thirty
                    PlayerScore.Thirty -> PlayerScore.Forty
                    else -> scoringPlayerScore
                }

            val newState =
                if (userScored) {
                    state.copy(userScore = nextScore)
                } else {
                    state.copy(opponentScore = nextScore)
                }

            return newState.copy(announcement = generateAnnouncement(newState))
        }
    }
}
