package com.cse5236.routerivals.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cse5236.routerivals.R
import com.cse5236.routerivals.viewmodel.RouteViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import android.util.Log

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val routeViewModel: RouteViewModel by viewModels()

    private lateinit var distanceInput: EditText
    private lateinit var findRoutesButton: Button

    private var currentLocation: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        distanceInput = view.findViewById(R.id.input_distance)
        findRoutesButton = view.findViewById(R.id.button_find_routes)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findRoutesButton.setOnClickListener {
            val distanceText = distanceInput.text.toString()
            val distance = distanceText.toDoubleOrNull()

            if (distance == null || distance <= 0.0) {
                Toast.makeText(requireContext(), "Enter a valid distance", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val startLocation = currentLocation
            if (startLocation != null) {
                routeViewModel.findLoopRoutes(startLocation, distance)
            } else {
                Toast.makeText(requireContext(), "Getting location...", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getUserLocation()
        setupObservers()
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getUserLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        // Create a location request for a single, high-accuracy update
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            0L // 0 interval since we want only one update
        ).setMaxUpdates(1).build()

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                val location = locationResult.lastLocation
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    currentLocation = userLatLng
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun setupObservers() {
        routeViewModel.loopRoutes.observe(viewLifecycleOwner) { routes ->
            map.clear()
            for (route in routes) {
                // Decode polyline string into List<LatLng>
                val path: List<LatLng> = PolyUtil.decode(route.polyline)
                map.addPolyline(
                    PolylineOptions()
                        .addAll(path)
                        .color(resources.getColor(R.color.purple_700, null))
                        .width(8f)
                )
            }
        }
    }
}
