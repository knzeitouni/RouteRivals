package com.cse5236.routerivals.model

data class Route(
    val id: String = "",
    val startLatitude: Double,
    val startLongitude: Double,
    val distance: Double,
    val startAddress: String,
    val endAddress: String,
    val polyline: String = ""
)