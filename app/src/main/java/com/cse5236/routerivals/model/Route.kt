package com.cse5236.routerivals.model

data class Route(
    val startLatitude: Double,
    val startLongitude: Double,
    val polyline: String = ""
)
