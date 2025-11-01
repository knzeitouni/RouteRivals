package com.cse5236.routerivals.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val points_all_time: Int = 0,
    val points_weekly: Int = 0
)
