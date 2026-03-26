package com.nuttyknot.tennisscoretracker.announcements

import com.nuttyknot.tennisscoretracker.MatchState
import com.nuttyknot.tennisscoretracker.PlayerScore
import com.nuttyknot.tennisscoretracker.numberToWords

internal fun generateRallyAnnouncement(state: MatchState): String {
    val isNewGame =
        state.isNewSet &&
            state.userScore == PlayerScore.TiebreakScore(0) &&
            state.opponentScore == PlayerScore.TiebreakScore(0)
    return when {
        state.matchWinner != null -> generateRallyMatchWinAnnouncement(state)
        state.setWinner != null -> generateRallyGameWinAnnouncement(state)
        isNewGame -> generateRallyNewGameAnnouncement(state)
        else -> generateRallyPointAnnouncement(state)
    }
}

fun generateSideOutAnnouncement(state: MatchState): String {
    val serverScore = if (state.isUserServing) state.userScore else state.opponentScore
    val receiverScore = if (state.isUserServing) state.opponentScore else state.userScore
    return if (serverScore == receiverScore) {
        "Side out. ${serverScore.tts} All"
    } else {
        "Side out. ${serverScore.tts} ${receiverScore.tts}"
    }
}

private fun generateRallyMatchWinAnnouncement(state: MatchState): String {
    val historyStr =
        state.setHistory.joinToString(", ") {
            "${numberToWords(it.first)} ${numberToWords(it.second)}"
        }
    return "Match, ${state.matchWinner}. $historyStr"
}

private fun generateRallyGameWinAnnouncement(state: MatchState): String {
    val lastGame = state.setHistory.lastOrNull() ?: Pair(0, 0)
    val gameOrdinal = getOrdinalString(state.userSets + state.opponentSets)
    return "$gameOrdinal Game, ${state.setWinner}, " +
        "${numberToWords(lastGame.first)} ${numberToWords(lastGame.second)}"
}

private fun generateRallyNewGameAnnouncement(state: MatchState): String {
    val serverName =
        if (state.isUserServing) {
            state.userName.ifEmpty { "You" }
        } else {
            state.opponentName.ifEmpty { "Opponent" }
        }
    val gameOrdinal = getOrdinalString(state.userSets + state.opponentSets + 1)
    return "$gameOrdinal Game, $serverName to serve."
}

private fun generateRallyPointAnnouncement(state: MatchState): String {
    val serverScore = if (state.isUserServing) state.userScore else state.opponentScore
    val receiverScore = if (state.isUserServing) state.opponentScore else state.userScore
    return if (serverScore == receiverScore) {
        "${serverScore.tts} All"
    } else {
        "${serverScore.tts} ${receiverScore.tts}"
    }
}
