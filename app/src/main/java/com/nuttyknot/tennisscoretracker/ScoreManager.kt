package com.nuttyknot.tennisscoretracker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScoreManager {
    private val _matchState = MutableStateFlow(TennisMatchState())
    val matchState: StateFlow<TennisMatchState> = _matchState.asStateFlow()

    private val historyStack = ArrayDeque<TennisMatchState>()

    fun incrementUserScore() {
        historyStack.addLast(_matchState.value)
        _matchState.update { currentState ->
            calculateNextState(currentState, userScored = true)
        }
    }

    fun incrementOpponentScore() {
        historyStack.addLast(_matchState.value)
        _matchState.update { currentState ->
            calculateNextState(currentState, userScored = false)
        }
    }

    fun undo() {
        if (historyStack.isNotEmpty()) {
            val previousState = historyStack.removeLast()
            _matchState.value = previousState
        }
    }

    private fun calculateNextState(currentState: TennisMatchState, userScored: Boolean): TennisMatchState {
        val scoringPlayerScore = if (userScored) currentState.userScore else currentState.opponentScore
        val otherPlayerScore = if (userScored) currentState.opponentScore else currentState.userScore

        if (currentState.isDeuce) {
            return if (userScored) {
                currentState.copy(userScore = PlayerScore.Advantage, isDeuce = false)
            } else {
                currentState.copy(opponentScore = PlayerScore.Advantage, isDeuce = false)
            }
        }

        if (scoringPlayerScore is PlayerScore.Advantage) {
            // Wins game
            return winGame(currentState, userScored)
        }

        if (otherPlayerScore is PlayerScore.Advantage) {
            // Back to deuce
            return currentState.copy(userScore = PlayerScore.Forty, opponentScore = PlayerScore.Forty, isDeuce = true)
        }

        val nextScore = when (scoringPlayerScore) {
            PlayerScore.Love -> PlayerScore.Fifteen
            PlayerScore.Fifteen -> PlayerScore.Thirty
            PlayerScore.Thirty -> PlayerScore.Forty
            PlayerScore.Forty -> {
                // He already has 40. Does the other have 40?
                if (otherPlayerScore is PlayerScore.Forty) {
                    throw IllegalStateException("Should be in Deuce state instead of this.")
                }
                return winGame(currentState, userScored) // Wins game if other is < 40
            }
            PlayerScore.Advantage -> throw IllegalStateException("Handled above")
        }

        val newState = if (userScored) {
            currentState.copy(userScore = nextScore)
        } else {
            currentState.copy(opponentScore = nextScore)
        }

        if (newState.userScore is PlayerScore.Forty && newState.opponentScore is PlayerScore.Forty) {
            return newState.copy(isDeuce = true)
        }

        return newState
    }

    private fun winGame(currentState: TennisMatchState, userScored: Boolean): TennisMatchState {
        val newGames = if (userScored) currentState.userGames + 1 else currentState.opponentGames + 1
        
        // Simplified Set winning logic (First to 6 games, win by 2, or tiebreak at 6-6)
        // For simplicity in this demo, let's just increment games and see if a set is won if >= 6 and difference >= 2.
        val opponentGamesForCheck = if (userScored) currentState.opponentGames else currentState.opponentGames + 1
        val userGamesForCheck = if (userScored) currentState.userGames + 1 else currentState.userGames

        if (userGamesForCheck >= 6 && userGamesForCheck - opponentGamesForCheck >= 2) {
            return TennisMatchState(userSets = currentState.userSets + 1, opponentSets = currentState.opponentSets)
        } else if (opponentGamesForCheck >= 6 && opponentGamesForCheck - userGamesForCheck >= 2) {
            return TennisMatchState(userSets = currentState.userSets, opponentSets = currentState.opponentSets + 1)
        }
        
        // Otherwise, just new game
        return if (userScored) {
            currentState.copy(
                userScore = PlayerScore.Love,
                opponentScore = PlayerScore.Love,
                userGames = newGames,
                isDeuce = false
            )
        } else {
            currentState.copy(
                userScore = PlayerScore.Love,
                opponentScore = PlayerScore.Love,
                opponentGames = newGames,
                isDeuce = false
            )
        }
    }
}
