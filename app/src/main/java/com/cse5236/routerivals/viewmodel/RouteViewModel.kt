package com.cse5236.routerivals.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cse5236.routerivals.model.Route
import com.google.android.gms.maps.model.LatLng
import java.util.UUID
import kotlin.random.Random

class RouteViewModel : ViewModel() {

    private val _loopRoutes = MutableLiveData<List<Route>>()
    val loopRoutes: LiveData<List<Route>> get() = _loopRoutes

    fun findLoopRoutes(start: LatLng, distanceKm: Double) {
        // Generate 3-5 random routes
        val numberOfRoutes = Random.nextInt(3, 6)
        val routes = mutableListOf<Route>()

        for (i in 1..numberOfRoutes) {
            // Generate slightly varied distances
            val routeDistance = distanceKm + Random.nextDouble(-0.5, 0.5)

            // Generate addresses based on coordinates
            val startAddr = generateAddress(start)
            val endAddr = if (Random.nextBoolean()) {
                startAddr // Loop route returns to start
            } else {
                generateAddress(LatLng(
                    start.latitude + Random.nextDouble(-0.01, 0.01),
                    start.longitude + Random.nextDouble(-0.01, 0.01)
                ))
            }

            routes.add(
                Route(
                    id = UUID.randomUUID().toString(),
                    startLatitude = start.latitude,
                    startLongitude = start.longitude,
                    distance = routeDistance,
                    startAddress = startAddr,
                    endAddress = endAddr,
                    polyline = ""
                )
            )
        }

        Log.d("RouteViewModel", "Generated ${routes.size} routes")
        _loopRoutes.value = routes
    }

    private fun generateAddress(location: LatLng): String {
        // Generate mock street addresses
        val streets = listOf(
            "Main St", "Oak Ave", "Maple Dr", "Park Blvd", "College Rd",
            "High St", "Lane Ave", "Summit St", "Olentangy River Rd"
        )
        val number = Random.nextInt(100, 9999)
        return "$number ${streets.random()}"
    }
}