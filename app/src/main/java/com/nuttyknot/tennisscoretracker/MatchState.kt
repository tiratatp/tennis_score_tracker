package com.nuttyknot.tennisscoretracker

import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject

@Suppress("CyclomaticComplexMethod", "MagicNumber")
internal fun numberToWords(n: Int): String =
    when (n) {
        0 -> "Zero"
        1 -> "One"
        2 -> "Two"
        3 -> "Three"
        4 -> "Four"
        5 -> "Five"
        6 -> "Six"
        7 -> "Seven"
        8 -> "Eight"
        9 -> "Nine"
        10 -> "Ten"
        11 -> "Eleven"
        12 -> "Twelve"
        13 -> "Thirteen"
        14 -> "Fourteen"
        15 -> "Fifteen"
        16 -> "Sixteen"
        17 -> "Seventeen"
        18 -> "Eighteen"
        19 -> "Nineteen"
        20 -> "Twenty"
        in 21..29 -> "Twenty-${numberToWords(n - 20).lowercase()}"
        30 -> "Thirty"
        in 31..39 -> "Thirty-${numberToWords(n - 30).lowercase()}"
        40 -> "Forty"
        in 41..49 -> "Forty-${numberToWords(n - 40).lowercase()}"
        50 -> "Fifty"
        in 51..59 -> "Fifty-${numberToWords(n - 50).lowercase()}"
        60 -> "Sixty"
        in 61..69 -> "Sixty-${numberToWords(n - 60).lowercase()}"
        70 -> "Seventy"
        in 71..79 -> "Seventy-${numberToWords(n - 70).lowercase()}"
        80 -> "Eighty"
        in 81..89 -> "Eighty-${numberToWords(n - 80).lowercase()}"
        90 -> "Ninety"
        in 91..99 -> "Ninety-${numberToWords(n - 90).lowercase()}"
        else -> n.toString()
    }

sealed class PlayerScore(val display: String, val tts: String = display) {
    object Love : PlayerScore("0", "Love")

    object Fifteen : PlayerScore("15")

    object Thirty : PlayerScore("30")

    object Forty : PlayerScore("40")

    object Advantage : PlayerScore("AD", "Advantage")

    data class TiebreakScore(val points: Int) :
        PlayerScore(points.toString(), numberToWords(points))

    override fun toString(): String = display
}

data class MatchState(
    val userScore: PlayerScore = PlayerScore.Love,
    val opponentScore: PlayerScore = PlayerScore.Love,
    val userGames: Int = 0,
    val opponentGames: Int = 0,
    val userSets: Int = 0,
    val opponentSets: Int = 0,
    val isUserServing: Boolean = true,
    val userName: String = "",
    val opponentName: String = "",
    val gameWinner: String? = null,
    val setWinner: String? = null,
    val matchWinner: String? = null,
    /** Store (userGames, opponentGames) for completed sets */
    val setHistory: List<Pair<Int, Int>> = emptyList(),
    val isNewSet: Boolean = false,
    val announcement: String? = null,
    val matchFormat: MatchFormat = MatchFormat.STANDARD,
    val isMatchTiebreak: Boolean = false,
) {
    val isDeuce: Boolean
        get() = userScore == PlayerScore.Forty && opponentScore == PlayerScore.Forty

    val isScoreZero: Boolean
        get() =
            (userScore == PlayerScore.Love || userScore == PlayerScore.TiebreakScore(0)) &&
                (opponentScore == PlayerScore.Love || opponentScore == PlayerScore.TiebreakScore(0)) &&
                userGames == 0 &&
                opponentGames == 0 &&
                userSets == 0 &&
                opponentSets == 0 &&
                setHistory.isEmpty()
}

internal object ScoreSerialization {
    fun serialize(score: PlayerScore): String =
        when (score) {
            PlayerScore.Love -> "LOVE"
            PlayerScore.Fifteen -> "FIFTEEN"
            PlayerScore.Thirty -> "THIRTY"
            PlayerScore.Forty -> "FORTY"
            PlayerScore.Advantage -> "ADVANTAGE"
            is PlayerScore.TiebreakScore -> "TIEBREAK:${score.points}"
        }

    fun deserialize(value: String): PlayerScore =
        when {
            value == "LOVE" -> PlayerScore.Love
            value == "FIFTEEN" -> PlayerScore.Fifteen
            value == "THIRTY" -> PlayerScore.Thirty
            value == "FORTY" -> PlayerScore.Forty
            value == "ADVANTAGE" -> PlayerScore.Advantage
            value.startsWith("TIEBREAK:") -> {
                val points = value.removePrefix("TIEBREAK:").toIntOrNull() ?: 0
                PlayerScore.TiebreakScore(points)
            }
            else -> PlayerScore.Love
        }
}

fun MatchState.toBundle(): Bundle =
    Bundle().apply {
        putString("userScore", ScoreSerialization.serialize(userScore))
        putString("opponentScore", ScoreSerialization.serialize(opponentScore))
        putInt("userGames", userGames)
        putInt("opponentGames", opponentGames)
        putInt("userSets", userSets)
        putInt("opponentSets", opponentSets)
        putBoolean("isUserServing", isUserServing)
        putString("userName", userName)
        putString("opponentName", opponentName)
        putString("matchFormat", matchFormat.name)
        putBoolean("isMatchTiebreak", isMatchTiebreak)
        putIntArray("setHistoryUser", setHistory.map { it.first }.toIntArray())
        putIntArray("setHistoryOpp", setHistory.map { it.second }.toIntArray())
    }

fun matchStateFromBundle(bundle: Bundle): MatchState {
    val userScores = bundle.getIntArray("setHistoryUser") ?: intArrayOf()
    val oppScores = bundle.getIntArray("setHistoryOpp") ?: intArrayOf()
    val setHistory = userScores.zip(oppScores).map { it.first to it.second }

    return MatchState(
        userScore = ScoreSerialization.deserialize(bundle.getString("userScore", "LOVE")),
        opponentScore = ScoreSerialization.deserialize(bundle.getString("opponentScore", "LOVE")),
        userGames = bundle.getInt("userGames"),
        opponentGames = bundle.getInt("opponentGames"),
        userSets = bundle.getInt("userSets"),
        opponentSets = bundle.getInt("opponentSets"),
        isUserServing = bundle.getBoolean("isUserServing", true),
        userName = bundle.getString("userName", ""),
        opponentName = bundle.getString("opponentName", ""),
        matchFormat =
            try {
                MatchFormat.valueOf(bundle.getString("matchFormat", "STANDARD"))
            } catch (_: IllegalArgumentException) {
                MatchFormat.STANDARD
            },
        isMatchTiebreak = bundle.getBoolean("isMatchTiebreak", false),
        setHistory = setHistory,
    )
}

fun MatchState.toJsonString(): String =
    JSONObject().apply {
        put("userScore", ScoreSerialization.serialize(userScore))
        put("opponentScore", ScoreSerialization.serialize(opponentScore))
        put("userGames", userGames)
        put("opponentGames", opponentGames)
        put("userSets", userSets)
        put("opponentSets", opponentSets)
        put("isUserServing", isUserServing)
        put("userName", userName)
        put("opponentName", opponentName)
        put("matchFormat", matchFormat.name)
        put("isMatchTiebreak", isMatchTiebreak)
        put(
            "setHistory",
            JSONArray().apply {
                setHistory.forEach { (user, opp) ->
                    put(
                        JSONArray().apply {
                            put(user)
                            put(opp)
                        },
                    )
                }
            },
        )
    }.toString()

fun matchStateFromJsonString(json: String): MatchState {
    val obj = JSONObject(json)
    val historyArray = obj.optJSONArray("setHistory")
    val setHistory =
        if (historyArray != null) {
            (0 until historyArray.length()).map { i ->
                val pair = historyArray.getJSONArray(i)
                pair.getInt(0) to pair.getInt(1)
            }
        } else {
            emptyList()
        }

    return MatchState(
        userScore = ScoreSerialization.deserialize(obj.optString("userScore", "LOVE")),
        opponentScore = ScoreSerialization.deserialize(obj.optString("opponentScore", "LOVE")),
        userGames = obj.optInt("userGames", 0),
        opponentGames = obj.optInt("opponentGames", 0),
        userSets = obj.optInt("userSets", 0),
        opponentSets = obj.optInt("opponentSets", 0),
        isUserServing = obj.optBoolean("isUserServing", true),
        userName = obj.optString("userName", ""),
        opponentName = obj.optString("opponentName", ""),
        matchFormat =
            try {
                MatchFormat.valueOf(obj.optString("matchFormat", "STANDARD"))
            } catch (_: IllegalArgumentException) {
                MatchFormat.STANDARD
            },
        isMatchTiebreak = obj.optBoolean("isMatchTiebreak", false),
        setHistory = setHistory,
    )
}
