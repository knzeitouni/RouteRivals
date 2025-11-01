package com.cse5236.routerivals.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val friends: List<String> = emptyList(),
    val scores: Map<ScoreType, Int> = emptyMap()
    //val completedRoutes: List<RouteCompleted>
)
