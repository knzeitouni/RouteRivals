package com.cse5236.routerivals.model

import java.util.Date

data class RouteCompleted (
    val score: Int,
    val timeCompleted: Date,
    val route: Route
)