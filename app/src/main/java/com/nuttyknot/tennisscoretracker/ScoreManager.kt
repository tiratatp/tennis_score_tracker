package com.nuttyknot.tennisscoretracker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScoreManager {
    private val _matchState = MutableStateFlow(TennisMatchState())
    val matchState: StateFlow<TennisMatchState> = _matchState.asStateFlow()

    private val historyStack = ArrayDeque<TennisMatchState>()

    private var currentUserName = ""
    private var currentOpponentName = ""
    private var initialServerIsUser = true

    fun updateNames(user: String, opponent: String) {
        currentUserName = user
        currentOpponentName = opponent
        _matchState.update { it.copy(
            userName = user,
            opponentName = opponent,
            gameWinner = null,
            setWinner = null,
            matchWinner = null,
            isNewSet = false,
            announcement = null
        ) }
    }

    fun updateInitialServer(isUser: Boolean) {
        initialServerIsUser = isUser
        if (historyStack.isEmpty() && _matchState.value.userGames == 0 && _matchState.value.opponentGames == 0 && _matchState.value.userScore == PlayerScore.Love && _matchState.value.opponentScore == PlayerScore.Love) {
             _matchState.update { it.copy(
                 isUserServing = isUser,
                 gameWinner = null,
                 setWinner = null,
                 matchWinner = null,
                 isNewSet = false,
                 announcement = null
             ) }
        }
    }

    fun incrementUserScore() {
        if (_matchState.value.matchWinner != null) return // Match is over
        
        historyStack.addLast(_matchState.value)
        _matchState.update { currentState ->
            if (currentState.setWinner != null) {
                // If set is over, first click just starts next set
                val nextSetState = currentState.copy(
                    userGames = 0,
                    opponentGames = 0,
                    setWinner = null,
                    gameWinner = null,
                    isNewSet = true
                )
                nextSetState.copy(announcement = generateAnnouncement(nextSetState))
            } else {
                calculateNextState(currentState, userScored = true)
            }
        }
    }

    fun incrementOpponentScore() {
        if (_matchState.value.matchWinner != null) return // Match is over

        historyStack.addLast(_matchState.value)
        _matchState.update { currentState ->
            if (currentState.setWinner != null) {
                // If set is over, first click just starts next set
                val nextSetState = currentState.copy(
                    userGames = 0,
                    opponentGames = 0,
                    setWinner = null,
                    gameWinner = null,
                    isNewSet = true
                )
                nextSetState.copy(announcement = generateAnnouncement(nextSetState))
            } else {
                calculateNextState(currentState, userScored = false)
            }
        }
    }

    fun undo() {
        if (historyStack.isNotEmpty()) {
             val previousState = historyStack.removeLast()
             _matchState.value = previousState.copy(announcement = null)
        }
    }

    fun reset() {
        historyStack.clear()
        val newState = TennisMatchState(
            userName = currentUserName,
            opponentName = currentOpponentName,
            isUserServing = initialServerIsUser,
            announcement = null
        )
        _matchState.value = newState
    }

    private fun calculateNextState(currentState: TennisMatchState, userScored: Boolean): TennisMatchState {
        // Clear previous winner announcement
        val baseState = currentState.copy(
            gameWinner = null,
            setWinner = null,
            matchWinner = null,
            isNewSet = false,
            announcement = null
        )
        val scoringPlayerScore = if (userScored) baseState.userScore else baseState.opponentScore
        val otherPlayerScore = if (userScored) baseState.opponentScore else baseState.userScore

        if (baseState.isDeuce) {
            val nextState = if (userScored) {
                baseState.copy(userScore = PlayerScore.Advantage, isDeuce = false)
            } else {
                baseState.copy(opponentScore = PlayerScore.Advantage, isDeuce = false)
            }
            return nextState.copy(announcement = generateAnnouncement(nextState))
        }

        if (scoringPlayerScore is PlayerScore.Advantage) {
            // Wins game
            return winGame(baseState, userScored)
        }

        if (otherPlayerScore is PlayerScore.Advantage) {
            // Back to deuce
            val nextState = baseState.copy(userScore = PlayerScore.Forty, opponentScore = PlayerScore.Forty, isDeuce = true)
            return nextState.copy(announcement = generateAnnouncement(nextState))
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
                return winGame(baseState, userScored) // Wins game if other is < 40
            }
            PlayerScore.Advantage -> throw IllegalStateException("Handled above")
        }

        val newState = if (userScored) {
            baseState.copy(userScore = nextScore)
        } else {
            baseState.copy(opponentScore = nextScore)
        }

        if (newState.userScore is PlayerScore.Forty && newState.opponentScore is PlayerScore.Forty) {
            val deuceState = newState.copy(isDeuce = true)
            return deuceState.copy(announcement = generateAnnouncement(deuceState))
        }

        return newState.copy(announcement = generateAnnouncement(newState))
    }

    private fun winGame(currentState: TennisMatchState, userScored: Boolean): TennisMatchState {
        val opponentGamesForCheck = if (userScored) currentState.opponentGames else currentState.opponentGames + 1
        val userGamesForCheck = if (userScored) currentState.userGames + 1 else currentState.userGames

        val winnerName = if (userScored) {
            if (currentState.userName.isEmpty()) "User" else currentState.userName
        } else {
            if (currentState.opponentName.isEmpty()) "Opponent" else currentState.opponentName
        }

        // Check if set is won
        if ((userGamesForCheck >= 6 && userGamesForCheck - opponentGamesForCheck >= 2) || (userGamesForCheck == 7)) {
            val newSetHistory = currentState.setHistory + (userGamesForCheck to opponentGamesForCheck)
            val newUserSets = currentState.userSets + 1
            
            // Check if match is won (Best of 3)
            if (newUserSets >= 2) {
                val matchWonState = currentState.copy(
                    userScore = PlayerScore.Love,
                    opponentScore = PlayerScore.Love,
                    userGames = userGamesForCheck,
                    opponentGames = opponentGamesForCheck,
                    userSets = newUserSets,
                    setHistory = newSetHistory,
                    matchWinner = winnerName
                )
                return matchWonState.copy(announcement = generateAnnouncement(matchWonState))
            }
            
            val setWonState = currentState.copy(
                userScore = PlayerScore.Love,
                opponentScore = PlayerScore.Love,
                userGames = userGamesForCheck,
                opponentGames = opponentGamesForCheck,
                userSets = newUserSets,
                setHistory = newSetHistory,
                setWinner = winnerName,
                isNewSet = true,
                isUserServing = !currentState.isUserServing // Swap for next set
            )
            return setWonState.copy(announcement = generateAnnouncement(setWonState))
        } else if ((opponentGamesForCheck >= 6 && opponentGamesForCheck - userGamesForCheck >= 2) || (opponentGamesForCheck == 7)) {
            val newSetHistory = currentState.setHistory + (userGamesForCheck to opponentGamesForCheck)
            val newOpponentSets = currentState.opponentSets + 1
            
            // Check if match is won (Best of 3)
            if (newOpponentSets >= 2) {
                val matchWonState = currentState.copy(
                    userScore = PlayerScore.Love,
                    opponentScore = PlayerScore.Love,
                    userGames = userGamesForCheck,
                    opponentGames = opponentGamesForCheck,
                    opponentSets = newOpponentSets,
                    setHistory = newSetHistory,
                    matchWinner = winnerName
                )
                return matchWonState.copy(announcement = generateAnnouncement(matchWonState))
            }
            
            val setWonState = currentState.copy(
                userScore = PlayerScore.Love,
                opponentScore = PlayerScore.Love,
                userGames = userGamesForCheck,
                opponentGames = opponentGamesForCheck,
                opponentSets = newOpponentSets,
                setHistory = newSetHistory,
                setWinner = winnerName,
                isNewSet = true,
                isUserServing = !currentState.isUserServing // Swap for next set
            )
            return setWonState.copy(announcement = generateAnnouncement(setWonState))
        }
        
        // Otherwise, just new game
        val gameWonState = currentState.copy(
            userScore = PlayerScore.Love,
            opponentScore = PlayerScore.Love,
            userGames = userGamesForCheck,
            opponentGames = opponentGamesForCheck,
            isDeuce = false,
            isUserServing = !currentState.isUserServing, // Swap server after each game
            gameWinner = winnerName
        )
        return gameWonState.copy(announcement = generateAnnouncement(gameWonState))
    }

    fun startNextSet() {
        _matchState.update { currentState ->
            if (currentState.setWinner != null) {
                val nextSetState = currentState.copy(
                    userGames = 0,
                    opponentGames = 0,
                    setWinner = null,
                    gameWinner = null,
                    isNewSet = true // Keep true for "Play" announcement
                )
                nextSetState.copy(announcement = generateAnnouncement(nextSetState))
            } else {
                currentState
            }
        }
    }
}
