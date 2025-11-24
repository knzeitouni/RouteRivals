package com.cse5236.routerivals.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cse5236.routerivals.R
import com.cse5236.routerivals.adapters.RouteAdapter
import com.cse5236.routerivals.model.Route
import com.cse5236.routerivals.viewmodel.RouteViewModel
import com.cse5236.routerivals.viewmodel.UserViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class HomeFragment : Fragment() {

    private val TAG = "HomeFragment"

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val routeViewModel: RouteViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var distanceInput: EditText
    private lateinit var findRoutesButton: Button
    private lateinit var routesRecyclerView: RecyclerView
    private lateinit var routeAdapter: RouteAdapter

    private var currentLocation: LatLng? = null

    private val LOCATION_REQUEST_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        distanceInput = view.findViewById(R.id.input_distance)
        findRoutesButton = view.findViewById(R.id.button_find_routes)
        routesRecyclerView = view.findViewById(R.id.recycler_routes)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Setup RecyclerView with click listener
        routeAdapter = RouteAdapter { route ->
            onRouteAccepted(route)
        }
        routesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        routesRecyclerView.adapter = routeAdapter

        // Get location
        requestLocationOrGetIt()

        findRoutesButton.setOnClickListener {
            val distanceText = distanceInput.text.toString().trim()
            val distance = distanceText.toDoubleOrNull()

            if (distance == null || distance <= 0.0) {
                Toast.makeText(requireContext(), "Enter a valid distance", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val startLocation = currentLocation
            if (startLocation != null) {
                Log.d(TAG, "Finding routes from $startLocation with distance $distance km")
                routeViewModel.findLoopRoutes(startLocation, distance)
            } else {
                Toast.makeText(requireContext(), "Getting location...", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Tried to find routes but currentLocation is null")
            }
        }

        setupObservers()

        return view
    }

    private fun onRouteAccepted(route: Route) {
        AlertDialog.Builder(requireContext())
            .setTitle("Accept Route?")
            .setMessage("Accept this ${String.format("%.1f", route.distance)} km route?\n\nYou'll earn ${calculatePoints(route.distance)} points!")
            .setPositiveButton("Accept") { dialog, _ ->
                // Calculate points based on distance
                val points = calculatePoints(route.distance)

                // Add points to user
                userViewModel.addPointsToCurrentUser(points)

                Toast.makeText(
                    requireContext(),
                    "Route accepted! You earned $points points!",
                    Toast.LENGTH_LONG
                ).show()

                // Clear the routes list
                routeAdapter.submitList(emptyList())
                distanceInput.text.clear()

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun calculatePoints(distance: Double): Int {
        // Award 10 points per km, rounded
        return (distance * 10).toInt()
    }

    private fun requestLocationOrGetIt() {
        val ctx = requireContext()
        val hasFine = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        } else {
            getUserLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    currentLocation = userLatLng
                    Log.d(TAG, "Got lastLocation: $userLatLng")
                } else {
                    Log.w(TAG, "lastLocation is null, using default location")
                    val defaultLocation = LatLng(40.0017, -83.0197)
                    currentLocation = defaultLocation
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting location: ${e.message}", e)
                val defaultLocation = LatLng(40.0017, -83.0197)
                currentLocation = defaultLocation
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getUserLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Location permission needed to find routes near you.",
                Toast.LENGTH_LONG
            ).show()
            Log.w(TAG, "Location permission denied")

            val defaultLocation = LatLng(40.0017, -83.0197)
            currentLocation = defaultLocation
        }
    }

    private fun setupObservers() {
        routeViewModel.loopRoutes.observe(viewLifecycleOwner) { routes ->
            Log.d(TAG, "Received ${routes.size} routes from ViewModel")

            if (routes.isEmpty()) {
                Toast.makeText(requireContext(), "No routes found", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Found ${routes.size} route(s)", Toast.LENGTH_SHORT).show()
                routeAdapter.submitList(routes)
            }
        }
    }
}