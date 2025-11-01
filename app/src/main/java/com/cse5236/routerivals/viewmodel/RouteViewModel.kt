package com.cse5236.routerivals.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cse5236.routerivals.model.Route
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil


class RouteViewModel : ViewModel() {

    private val _loopRoutes = MutableLiveData<List<Route>>()
    val loopRoutes: LiveData<List<Route>> get() = _loopRoutes

    fun findLoopRoutes(start: LatLng, distanceKm: Double) {
        // TODO: Replace this with algorithmic route fetching
        val path = listOf(
            start,
            LatLng(start.latitude + 0.05, start.longitude + 0.05),
            LatLng(start.latitude, start.longitude + 0.05),
            start
        )

        val encodedPolyline = PolyUtil.encode(path)
        val fakeRoute = Route(
            startLatitude = start.latitude,
            startLongitude = start.longitude,
            polyline = encodedPolyline
        )
        _loopRoutes.value = listOf(fakeRoute)
    }
}
