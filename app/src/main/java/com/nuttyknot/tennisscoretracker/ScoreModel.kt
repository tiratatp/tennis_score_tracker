package com.nuttyknot.tennisscoretracker

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScoreModel : ViewModel() {
    private val _matchState = MutableStateFlow(TennisMatchState())
    val matchState: StateFlow<TennisMatchState> = _matchState.asStateFlow()

    private val historyStack = ArrayDeque<TennisMatchState>()
    private val processor = Processor()

    private data class FormatConfig(
        val gamesToWinSet: Int,
        val gamesToWinSetLong: Int,
        val setsToWinMatch: Int,
        val useAdvantageScoring: Boolean,
        val useMatchTiebreak: Boolean,
        val matchTiebreakPoints: Int,
        val regularTiebreakPoints: Int,
    )

    private data class MatchConfig(
        val userName: String = "",
        val opponentName: String = "",
        val initialServerIsUser: Boolean = true,
        val matchFormat: MatchFormat = MatchFormat.STANDARD,
    )

    private var config = MatchConfig()

    private val formatConfig: FormatConfig
        get() =
            when (config.matchFormat) {
                MatchFormat.STANDARD ->
                    FormatConfig(
                        gamesToWinSet = GAMES_TO_WIN_SET_STANDARD,
                        gamesToWinSetLong = GAMES_TO_WIN_SET_STANDARD + 1,
                        setsToWinMatch = SETS_TO_WIN_MATCH_STANDARD,
                        useAdvantageScoring = true,
                        useMatchTiebreak = false,
                        matchTiebreakPoints = MATCH_TIEBREAK_POINTS,
                        regularTiebreakPoints = REGULAR_TIEBREAK_POINTS,
                    )
                MatchFormat.LEAGUE ->
                    FormatConfig(
                        gamesToWinSet = GAMES_TO_WIN_SET_STANDARD,
                        gamesToWinSetLong = GAMES_TO_WIN_SET_STANDARD + 1,
                        setsToWinMatch = SETS_TO_WIN_MATCH_STANDARD,
                        useAdvantageScoring = true,
                        useMatchTiebreak = true,
                        matchTiebreakPoints = MATCH_TIEBREAK_POINTS,
                        regularTiebreakPoints = REGULAR_TIEBREAK_POINTS,
                    )
                MatchFormat.FAST ->
                    FormatConfig(
                        gamesToWinSet = GAMES_TO_WIN_SET_FAST,
                        gamesToWinSetLong = GAMES_TO_WIN_SET_FAST + 1,
                        setsToWinMatch = 1,
                        useAdvantageScoring = false,
                        useMatchTiebreak = false,
                        matchTiebreakPoints = MATCH_TIEBREAK_POINTS,
                        regularTiebreakPoints = REGULAR_TIEBREAK_POINTS,
                    )
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
            currentState.copy(
                userName = userName ?: currentState.userName,
                opponentName = opponentName ?: currentState.opponentName,
                isUserServing = if (canUpdateServer) initialServerIsUser!! else currentState.isUserServing,
                matchFormat = if (canUpdateFormat) matchFormat!! else currentState.matchFormat,
            )
        }
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
                matchFormat = config.matchFormat,
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
            baseState: TennisMatchState,
            userScored: Boolean,
        ): TennisMatchState {
            val fc = formatConfig

            if (baseState.isMatchTiebreak) {
                return handleTiebreakScoring(baseState, userScored, fc.matchTiebreakPoints)
            }

            val scoringPlayerScore = if (userScored) baseState.userScore else baseState.opponentScore
            val otherPlayerScore = if (userScored) baseState.opponentScore else baseState.userScore

            return when {
                baseState.userGames == fc.gamesToWinSet &&
                    baseState.opponentGames == fc.gamesToWinSet ->
                    handleTiebreakScoring(baseState, userScored, fc.regularTiebreakPoints)
                baseState.isDeuce && !fc.useAdvantageScoring -> winGame(baseState, userScored)
                baseState.isDeuce -> ScoringLogic.handleDeuceScoring(baseState, userScored)
                scoringPlayerScore is PlayerScore.Advantage -> winGame(baseState, userScored)
                otherPlayerScore is PlayerScore.Advantage -> ScoringLogic.handleBackToDeuce(baseState)
                scoringPlayerScore == PlayerScore.Forty -> winGame(baseState, userScored)
                else -> ScoringLogic.handleRegularScoring(baseState, userScored, scoringPlayerScore)
            }
        }

        fun prepareNextSet(currentState: TennisMatchState): TennisMatchState {
            val fc = formatConfig
            val isMatchTiebreak =
                fc.useMatchTiebreak &&
                    currentState.userSets == 1 && currentState.opponentSets == 1
            val initialScore = if (isMatchTiebreak) PlayerScore.TiebreakScore(0) else PlayerScore.Love

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

        private fun handleTiebreakScoring(
            state: TennisMatchState,
            userScored: Boolean,
            winThreshold: Int,
        ): TennisMatchState {
            val userCurrent = (state.userScore as? PlayerScore.TiebreakScore)?.points ?: 0
            val oppCurrent = (state.opponentScore as? PlayerScore.TiebreakScore)?.points ?: 0

            val newUserPoints = if (userScored) userCurrent + 1 else userCurrent
            val newOppPoints = if (userScored) oppCurrent else oppCurrent + 1

            val isTiebreakWon =
                (
                    newUserPoints >= winThreshold ||
                        newOppPoints >= winThreshold
                ) &&
                    kotlin.math.abs(newUserPoints - newOppPoints) >= GAME_DIFFERENCE_FOR_SET

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
            val fc = formatConfig
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
                    userGamesForCheck == fc.gamesToWinSet &&
                        opponentGamesForCheck == fc.gamesToWinSet
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
            userGames: Int,
            opponentGames: Int,
        ): Boolean {
            val fc = formatConfig
            val higher = maxOf(userGames, opponentGames)
            val diff = kotlin.math.abs(userGames - opponentGames)
            return (higher >= fc.gamesToWinSet && diff >= GAME_DIFFERENCE_FOR_SET) ||
                higher == fc.gamesToWinSetLong
        }

        private fun handleSetWin(
            currentState: TennisMatchState,
            userGames: Int,
            opponentGames: Int,
            isUserWinner: Boolean,
            winnerName: String,
        ): TennisMatchState {
            val fc = formatConfig
            val newSetHistory = currentState.setHistory + (userGames to opponentGames)
            val newUserSets = if (isUserWinner) currentState.userSets + 1 else currentState.userSets
            val newOpponentSets = if (isUserWinner) currentState.opponentSets else currentState.opponentSets + 1

            val isMatchWon =
                newUserSets >= fc.setsToWinMatch ||
                    newOpponentSets >= fc.setsToWinMatch

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
            when (currentPointIndex % TIEBREAK_SERVER_CYCLE) {
                0, TIEBREAK_CYCLE_SAME_END -> currentServer
                else -> !currentServer
            }

        private fun handleTiebreakWin(
            state: TennisMatchState,
            newUserPoints: Int,
            newOppPoints: Int,
            totalOldPoints: Int,
            winnerName: String,
        ): TennisMatchState {
            val isUserWinner = newUserPoints > newOppPoints
            val tiebreakFirstServer = initialTiebreakServer(state.isUserServing, totalOldPoints)

            val userGamesFinal: Int
            val oppGamesFinal: Int
            if (state.isMatchTiebreak) {
                userGamesFinal = if (isUserWinner) 1 else 0
                oppGamesFinal = if (isUserWinner) 0 else 1
            } else {
                val fc = formatConfig
                userGamesFinal = if (isUserWinner) fc.gamesToWinSetLong else fc.gamesToWinSet
                oppGamesFinal = if (isUserWinner) fc.gamesToWinSet else fc.gamesToWinSetLong
            }

            return handleSetWin(
                state.copy(isUserServing = tiebreakFirstServer),
                userGamesFinal,
                oppGamesFinal,
                isUserWinner,
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

    companion object {
        private const val GAMES_TO_WIN_SET_STANDARD = 6
        private const val GAMES_TO_WIN_SET_FAST = 8
        private const val GAME_DIFFERENCE_FOR_SET = 2
        private const val SETS_TO_WIN_MATCH_STANDARD = 2
        private const val REGULAR_TIEBREAK_POINTS = 7
        private const val MATCH_TIEBREAK_POINTS = 10
        private const val TIEBREAK_SERVER_CYCLE = 4
        private const val TIEBREAK_CYCLE_SAME_END = 3
    }
}
