package com.nuttyknot.tennisscoretracker.shared

import org.json.JSONArray
import org.json.JSONObject

data class WearScoreDisplay(
    val userName: String = "",
    val opponentName: String = "",
    val userScore: String = "0",
    val opponentScore: String = "0",
    val userGames: Int = 0,
    val opponentGames: Int = 0,
    val userSets: Int = 0,
    val opponentSets: Int = 0,
    val setHistory: List<Pair<Int, Int>> = emptyList(),
    val isUserServing: Boolean = true,
    val isMatchOver: Boolean = false,
    val matchWinner: String? = null,
) {
    fun toJson(): String {
        val obj = JSONObject()
        obj.put("userName", userName)
        obj.put("opponentName", opponentName)
        obj.put("userScore", userScore)
        obj.put("opponentScore", opponentScore)
        obj.put("userGames", userGames)
        obj.put("opponentGames", opponentGames)
        obj.put("userSets", userSets)
        obj.put("opponentSets", opponentSets)
        val historyArray = JSONArray()
        for ((u, o) in setHistory) {
            val pair = JSONObject()
            pair.put("user", u)
            pair.put("opponent", o)
            historyArray.put(pair)
        }
        obj.put("setHistory", historyArray)
        obj.put("isUserServing", isUserServing)
        obj.put("isMatchOver", isMatchOver)
        if (matchWinner != null) {
            obj.put("matchWinner", matchWinner)
        }
        return obj.toString()
    }

    companion object {
        fun fromJson(json: String): WearScoreDisplay {
            val obj = JSONObject(json)
            val historyArray = obj.optJSONArray("setHistory")
            val history = mutableListOf<Pair<Int, Int>>()
            if (historyArray != null) {
                for (i in 0 until historyArray.length()) {
                    val pair = historyArray.getJSONObject(i)
                    history.add(pair.getInt("user") to pair.getInt("opponent"))
                }
            }
            return WearScoreDisplay(
                userName = obj.optString("userName", ""),
                opponentName = obj.optString("opponentName", ""),
                userScore = obj.optString("userScore", "0"),
                opponentScore = obj.optString("opponentScore", "0"),
                userGames = obj.optInt("userGames", 0),
                opponentGames = obj.optInt("opponentGames", 0),
                userSets = obj.optInt("userSets", 0),
                opponentSets = obj.optInt("opponentSets", 0),
                setHistory = history,
                isUserServing = obj.optBoolean("isUserServing", true),
                isMatchOver = obj.optBoolean("isMatchOver", false),
                matchWinner = obj.optString("matchWinner", "").ifEmpty { null },
            )
        }
    }
}
