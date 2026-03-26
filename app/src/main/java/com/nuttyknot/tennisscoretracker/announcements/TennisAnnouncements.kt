package com.nuttyknot.tennisscoretracker.announcements

import com.nuttyknot.tennisscoretracker.MatchState
import com.nuttyknot.tennisscoretracker.PlayerScore

internal fun generateTennisAnnouncement(state: MatchState): String {
    val isNewSetLoveAll =
        state.isNewSet && state.userScore == PlayerScore.Love &&
            state.opponentScore == PlayerScore.Love
    return when {
        state.matchWinner != null -> generateMatchWinningAnnouncement(state)
        state.setWinner != null -> generateSetWinningAnnouncement(state)
        isNewSetLoveAll -> generateNewSetAnnouncement(state)
        state.gameWinner != null -> generateGameWinningAnnouncement(state)
        else -> generatePointScoreAnnouncement(state)
    }
}

private fun generateMatchWinningAnnouncement(state: MatchState): String {
    val historyStr = state.setHistory.joinToString(", ") { "${it.first} ${it.second}" }
    return "Game, Set, and Match, ${state.matchWinner}. $historyStr"
}

private fun generateSetWinningAnnouncement(state: MatchState): String {
    val lastSet = state.setHistory.lastOrNull() ?: Pair(0, 0)
    val scoreString =
        if (lastSet.first > lastSet.second) {
            "${lastSet.first} ${lastSet.second}"
        } else {
            "${lastSet.second} ${lastSet.first}"
        }
    val setOrdinal = getOrdinalString(state.userSets + state.opponentSets)
    return "Game and $setOrdinal Set, ${state.setWinner}, $scoreString"
}

private fun generateNewSetAnnouncement(state: MatchState): String {
    val serverName =
        if (state.isUserServing) {
            state.userName.ifEmpty { "You" }
        } else {
            state.opponentName.ifEmpty { "Opponent" }
        }
    return if (state.isMatchTiebreak) {
        "Match Tiebreak, $serverName to serve"
    } else {
        val setOrdinal = getOrdinalString(state.userSets + state.opponentSets + 1)
        "$setOrdinal Set, $serverName to serve. Play."
    }
}

private fun generateGameWinningAnnouncement(state: MatchState): String {
    val summary =
        if (state.userGames == state.opponentGames) {
            "Games are ${state.userGames} All"
        } else {
            val isUserLeading = state.userGames > state.opponentGames
            val leaderName =
                if (isUserLeading) {
                    state.userName.ifEmpty { "You" }
                } else {
                    state.opponentName.ifEmpty { "Opponent" }
                }
            val leads = if (isUserLeading) state.userGames else state.opponentGames
            val trailing = if (isUserLeading) state.opponentGames else state.userGames
            val gameWord = if (leads == 1) "game" else "games"
            "$leaderName leads $leads $gameWord to $trailing"
        }
    return "Game, ${state.gameWinner}. $summary"
}

private fun generatePointScoreAnnouncement(state: MatchState): String {
    if (state.isDeuce) return "Deuce"

    val serverScore = if (state.isUserServing) state.userScore else state.opponentScore
    val receiverScore = if (state.isUserServing) state.opponentScore else state.userScore

    return when {
        serverScore is PlayerScore.Advantage -> {
            val name = if (state.isUserServing) state.userName else state.opponentName
            if (name.trim().isEmpty()) "Ad-In" else "Advantage, $name"
        }
        receiverScore is PlayerScore.Advantage -> {
            val name = if (state.isUserServing) state.opponentName else state.userName
            if (name.trim().isEmpty()) "Ad-Out" else "Advantage, $name"
        }
        serverScore == receiverScore -> "${serverScore.tts} All"
        else -> "${serverScore.tts} ${receiverScore.tts}"
    }
}
