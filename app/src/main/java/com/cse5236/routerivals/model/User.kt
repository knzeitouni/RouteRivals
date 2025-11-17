package com.cse5236.routerivals.model

data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    val friends: List<String> = emptyList(),
    val incomingRequests: List<String> = emptyList(),
    val outgoingRequests: List<String> = emptyList(),
    val scores: Map<String, Int> = emptyMap(),
    val completedRoutes: List<RouteCompleted> = emptyList()
)
