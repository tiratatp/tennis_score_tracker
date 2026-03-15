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
}

class ScoreManager {
    private val _matchState = MutableStateFlow(TennisMatchState())
    val matchState: StateFlow<TennisMatchState> = _matchState.asStateFlow()

    private val historyStack = ArrayDeque<TennisMatchState>()
    private val processor = Processor()

    private var currentUserName = ""
    private var currentOpponentName = ""
    private var initialServerIsUser = true

    fun updateNames(
        user: String,
        opponent: String,
    ) {
        currentUserName = user
        currentOpponentName = opponent
        _matchState.update {
            it.copy(
                userName = user,
                opponentName = opponent,
                gameWinner = null,
                setWinner = null,
                matchWinner = null,
                isNewSet = false,
                announcement = null,
            )
        }
    }

    fun updateInitialServer(isUser: Boolean) {
        initialServerIsUser = isUser
        if (canUpdateInitialServer()) {
            _matchState.update {
                it.copy(
                    isUserServing = isUser,
                    gameWinner = null,
                    setWinner = null,
                    matchWinner = null,
                    isNewSet = false,
                    announcement = null,
                )
            }
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

    fun incrementUserScore() {
        if (_matchState.value.matchWinner != null) return
        historyStack.addLast(_matchState.value)
        _matchState.update { processor.calculateNextState(it, userScored = true) }
    }

    fun incrementOpponentScore() {
        if (_matchState.value.matchWinner != null) return
        historyStack.addLast(_matchState.value)
        _matchState.update { processor.calculateNextState(it, userScored = false) }
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
                userName = currentUserName,
                opponentName = currentOpponentName,
                isUserServing = initialServerIsUser,
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
                baseState.isDeuce -> handleDeuceScoring(baseState, userScored)
                scoringPlayerScore is PlayerScore.Advantage -> winGame(baseState, userScored)
                otherPlayerScore is PlayerScore.Advantage -> handleBackToDeuce(baseState)
                scoringPlayerScore == PlayerScore.Forty -> winGame(baseState, userScored)
                else -> handleRegularScoring(baseState, userScored, scoringPlayerScore)
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

        private fun handleDeuceScoring(
            state: TennisMatchState,
            userScored: Boolean,
        ): TennisMatchState {
            val nextState =
                if (userScored) {
                    state.copy(userScore = PlayerScore.Advantage, isDeuce = false)
                } else {
                    state.copy(opponentScore = PlayerScore.Advantage, isDeuce = false)
                }
            return nextState.copy(announcement = generateAnnouncement(nextState))
        }

        private fun handleBackToDeuce(state: TennisMatchState): TennisMatchState {
            val nextState =
                state.copy(
                    userScore = PlayerScore.Forty,
                    opponentScore = PlayerScore.Forty,
                    isDeuce = true,
                )
            return nextState.copy(announcement = generateAnnouncement(nextState))
        }

        private fun handleRegularScoring(
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

            val finalState =
                if (newState.userScore == PlayerScore.Forty &&
                    newState.opponentScore == PlayerScore.Forty
                ) {
                    newState.copy(isDeuce = true)
                } else {
                    newState
                }

            return finalState.copy(announcement = generateAnnouncement(finalState))
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

            return when {
                isSetWon(userGamesForCheck, opponentGamesForCheck) ->
                    handleSetWin(
                        currentState,
                        userGamesForCheck,
                        opponentGamesForCheck,
                        userScored,
                        winnerName,
                    )

                isSetWon(opponentGamesForCheck, userGamesForCheck) ->
                    handleSetWin(
                        currentState,
                        userGamesForCheck,
                        opponentGamesForCheck,
                        false,
                        winnerName,
                    )
                else -> {
                    val gameWonState =
                        currentState.copy(
                            userScore = PlayerScore.Love,
                            opponentScore = PlayerScore.Love,
                            userGames = userGamesForCheck,
                            opponentGames = opponentGamesForCheck,
                            isDeuce = false,
                            isUserServing = !currentState.isUserServing,
                            gameWinner = winnerName,
                        )
                    gameWonState.copy(announcement = generateAnnouncement(gameWonState))
                }
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
                    userScore = PlayerScore.Love, opponentScore = PlayerScore.Love,
                    userGames = userGames, opponentGames = opponentGames,
                    userSets = newUserSets, opponentSets = newOpponentSets,
                    setHistory = newSetHistory, matchWinner = if (isMatchWon) winnerName else null,
                    setWinner = if (!isMatchWon) winnerName else null, isNewSet = !isMatchWon,
                    isUserServing = !currentState.isUserServing,
                )
            return nextState.copy(announcement = generateAnnouncement(nextState))
        }
    }
}
