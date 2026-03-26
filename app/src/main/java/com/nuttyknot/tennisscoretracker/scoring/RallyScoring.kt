package com.nuttyknot.tennisscoretracker.scoring

import com.nuttyknot.tennisscoretracker.MatchState
import com.nuttyknot.tennisscoretracker.PlayerScore
import com.nuttyknot.tennisscoretracker.Sport
import com.nuttyknot.tennisscoretracker.announcements.generateAnnouncement
import com.nuttyknot.tennisscoretracker.announcements.generateSideOutAnnouncement

object RallyScoring {
    private const val GAME_DIFFERENCE_FOR_SET = 2

    fun calculateRallyScore(
        baseState: MatchState,
        userScored: Boolean,
        fc: FormatConfig,
        sport: Sport,
    ): MatchState {
        val isBadminton = sport == Sport.BADMINTON
        return applyRallyPoint(baseState, userScored, fc, sport) { state, scored ->
            if (isBadminton && scored != state.isUserServing) !state.isUserServing else state.isUserServing
        }
    }

    fun calculateSideOutScore(
        baseState: MatchState,
        userScored: Boolean,
        fc: FormatConfig,
    ): MatchState {
        val isServerScoring = userScored == baseState.isUserServing

        if (!isServerScoring) {
            val sideOutState = baseState.copy(isUserServing = !baseState.isUserServing)
            return sideOutState.copy(announcement = generateSideOutAnnouncement(sideOutState))
        }

        return applyRallyPoint(baseState, userScored, fc, Sport.PICKLEBALL)
    }

    private fun applyRallyPoint(
        baseState: MatchState,
        userScored: Boolean,
        fc: FormatConfig,
        sport: Sport,
        updateServing: (MatchState, Boolean) -> Boolean = { state, _ -> state.isUserServing },
    ): MatchState {
        val userCurrent = (baseState.userScore as? PlayerScore.TiebreakScore)?.points ?: 0
        val oppCurrent = (baseState.opponentScore as? PlayerScore.TiebreakScore)?.points ?: 0

        val newUserPts = if (userScored) userCurrent + 1 else userCurrent
        val newOppPts = if (userScored) oppCurrent else oppCurrent + 1

        if (isRallyGameWon(newUserPts, newOppPts, fc)) {
            val isUserWinner = newUserPts > newOppPts
            val winnerName =
                if (isUserWinner) {
                    baseState.userName.ifEmpty { "You" }
                } else {
                    baseState.opponentName.ifEmpty { "Opponent" }
                }
            return handleRallyGameWin(baseState, newUserPts, newOppPts, isUserWinner, winnerName, fc, sport)
        }

        val newServing = updateServing(baseState, userScored)
        return baseState.copy(
            userScore = PlayerScore.TiebreakScore(newUserPts),
            opponentScore = PlayerScore.TiebreakScore(newOppPts),
            isUserServing = newServing,
        ).let { it.copy(announcement = generateAnnouncement(it)) }
    }

    private fun isRallyGameWon(
        pts1: Int,
        pts2: Int,
        fc: FormatConfig,
    ): Boolean {
        val higher = maxOf(pts1, pts2)
        val diff = kotlin.math.abs(pts1 - pts2)
        if (fc.pointsCap > 0 && higher >= fc.pointsCap) return true
        return higher >= fc.pointsToWinGame && diff >= GAME_DIFFERENCE_FOR_SET
    }

    @Suppress("LongParameterList")
    private fun handleRallyGameWin(
        state: MatchState,
        userPts: Int,
        oppPts: Int,
        isUserWinner: Boolean,
        winnerName: String,
        fc: FormatConfig,
        sport: Sport,
    ): MatchState {
        val newSetHistory = state.setHistory + (userPts to oppPts)
        val newUserSets = if (isUserWinner) state.userSets + 1 else state.userSets
        val newOppSets = if (isUserWinner) state.opponentSets else state.opponentSets + 1

        val isMatchWon = newUserSets >= fc.setsToWinMatch || newOppSets >= fc.setsToWinMatch

        val nextState =
            state.copy(
                userScore = PlayerScore.TiebreakScore(0),
                opponentScore = PlayerScore.TiebreakScore(0),
                userGames = 0,
                opponentGames = 0,
                userSets = newUserSets,
                opponentSets = newOppSets,
                setHistory = newSetHistory,
                matchWinner = if (isMatchWon) winnerName else null,
                setWinner = if (!isMatchWon) winnerName else null,
                isNewSet = !isMatchWon,
                isUserServing = if (sport == Sport.BADMINTON) isUserWinner else !state.isUserServing,
            )
        return nextState.copy(announcement = generateAnnouncement(nextState))
    }
}
