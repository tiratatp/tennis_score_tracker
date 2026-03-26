package com.nuttyknot.tennisscoretracker.scoring

import com.nuttyknot.tennisscoretracker.MatchState
import com.nuttyknot.tennisscoretracker.PlayerScore
import com.nuttyknot.tennisscoretracker.announcements.generateAnnouncement

@Suppress("TooManyFunctions")
object TennisScoring {
    private const val GAME_DIFFERENCE_FOR_SET = 2
    private const val TIEBREAK_SERVER_CYCLE = 4
    private const val TIEBREAK_CYCLE_SAME_END = 3

    fun calculateScore(
        state: MatchState,
        userScored: Boolean,
        fc: FormatConfig,
    ): MatchState {
        val scoringPlayerScore = if (userScored) state.userScore else state.opponentScore
        val otherPlayerScore = if (userScored) state.opponentScore else state.userScore

        return when {
            state.userGames == fc.gamesToWinSet &&
                state.opponentGames == fc.gamesToWinSet ->
                handleTiebreakScoring(state, userScored, fc.regularTiebreakPoints, fc)
            state.isDeuce && !fc.useAdvantageScoring -> winGame(state, userScored, fc)
            state.isDeuce -> handleDeuceScoring(state, userScored)
            scoringPlayerScore is PlayerScore.Advantage -> winGame(state, userScored, fc)
            otherPlayerScore is PlayerScore.Advantage -> handleBackToDeuce(state)
            scoringPlayerScore == PlayerScore.Forty -> winGame(state, userScored, fc)
            else -> handleRegularScoring(state, userScored, scoringPlayerScore)
        }
    }

    fun handleTiebreakScoring(
        state: MatchState,
        userScored: Boolean,
        winThreshold: Int,
        fc: FormatConfig,
    ): MatchState {
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
                    state.userName.ifEmpty { "You" }
                } else {
                    state.opponentName.ifEmpty { "Opponent" }
                }

            handleTiebreakWin(
                state,
                newUserPoints,
                newOppPoints,
                totalOldPoints,
                winnerName,
                fc,
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
        currentState: MatchState,
        userScored: Boolean,
        fc: FormatConfig,
    ): MatchState {
        val userGamesForCheck = if (userScored) currentState.userGames + 1 else currentState.userGames
        val opponentGamesForCheck = if (userScored) currentState.opponentGames else currentState.opponentGames + 1
        val winnerName =
            if (userScored) {
                currentState.userName.ifEmpty { "You" }
            } else {
                currentState.opponentName.ifEmpty { "Opponent" }
            }

        return if (isSetWon(userGamesForCheck, opponentGamesForCheck, fc)) {
            handleSetWin(
                currentState,
                userGamesForCheck,
                opponentGamesForCheck,
                userScored,
                winnerName,
                fc,
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
        fc: FormatConfig,
    ): Boolean {
        val higher = maxOf(userGames, opponentGames)
        val diff = kotlin.math.abs(userGames - opponentGames)
        return (higher >= fc.gamesToWinSet && diff >= GAME_DIFFERENCE_FOR_SET) ||
            higher == fc.gamesToWinSetLong
    }

    @Suppress("LongParameterList")
    private fun handleSetWin(
        currentState: MatchState,
        userGames: Int,
        opponentGames: Int,
        isUserWinner: Boolean,
        winnerName: String,
        fc: FormatConfig,
    ): MatchState {
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

    @Suppress("LongParameterList")
    private fun handleTiebreakWin(
        state: MatchState,
        newUserPoints: Int,
        newOppPoints: Int,
        totalOldPoints: Int,
        winnerName: String,
        fc: FormatConfig,
    ): MatchState {
        val isUserWinner = newUserPoints > newOppPoints
        val tiebreakFirstServer = initialTiebreakServer(state.isUserServing, totalOldPoints)

        val userGamesFinal: Int
        val oppGamesFinal: Int
        if (state.isMatchTiebreak) {
            userGamesFinal = if (isUserWinner) 1 else 0
            oppGamesFinal = if (isUserWinner) 0 else 1
        } else {
            userGamesFinal = if (isUserWinner) fc.gamesToWinSetLong else fc.gamesToWinSet
            oppGamesFinal = if (isUserWinner) fc.gamesToWinSet else fc.gamesToWinSetLong
        }

        return handleSetWin(
            state.copy(isUserServing = tiebreakFirstServer),
            userGamesFinal,
            oppGamesFinal,
            isUserWinner,
            winnerName,
            fc,
        )
    }

    private fun handleTiebreakContinue(
        state: MatchState,
        newUserPoints: Int,
        newOppPoints: Int,
        totalOldPoints: Int,
    ): MatchState {
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

    private fun handleDeuceScoring(
        state: MatchState,
        userScored: Boolean,
    ): MatchState {
        val nextState =
            if (userScored) {
                state.copy(userScore = PlayerScore.Advantage)
            } else {
                state.copy(opponentScore = PlayerScore.Advantage)
            }
        return nextState.copy(announcement = generateAnnouncement(nextState))
    }

    private fun handleBackToDeuce(state: MatchState): MatchState {
        val nextState =
            state.copy(
                userScore = PlayerScore.Forty,
                opponentScore = PlayerScore.Forty,
            )
        return nextState.copy(announcement = generateAnnouncement(nextState))
    }

    private fun handleRegularScoring(
        state: MatchState,
        userScored: Boolean,
        scoringPlayerScore: PlayerScore,
    ): MatchState {
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
