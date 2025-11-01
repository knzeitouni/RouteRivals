package com.cse5236.routerivals.model

data class Leaderboard(
    val isGlobal: Boolean,
    val scoreType: ScoreType,
    val entries: List<LeaderboardEntry> = emptyList()
)
