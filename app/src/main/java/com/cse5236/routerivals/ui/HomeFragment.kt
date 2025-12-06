package com.cse5236.routerivals.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.cse5236.routerivals.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.math.cos
import kotlin.math.sin

class HomeFragment : Fragment(), OnMapReadyCallback {

    private val TAG = "HomeFragment"

    // Google Maps / location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var map: GoogleMap? = null
    private var currentLocation: LatLng? = null

    // UI
    private lateinit var distanceInput: EditText
    private lateinit var findRoutesButton: Button
    private lateinit var recenterButton: ImageButton
    private lateinit var completeRouteButton: MaterialButton

    // Route state
    private var loopPolyline: Polyline? = null
    private var lastRouteDistanceKm: Double? = null

    // Points / backend
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DEFAULT_ZOOM = 15f

        // 🔧 Change these to match your Firestore schema if needed
        private const val USER_COLLECTION = "users"
        private const val POINTS_FIELD = "totalPoints"
        private const val POINTS_PER_KM = 10 // e.g. 10 points per km
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Location client
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        // Map fragment
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // UI refs
        distanceInput = view.findViewById(R.id.input_distance)
        findRoutesButton = view.findViewById(R.id.button_find_routes)
        recenterButton = view.findViewById(R.id.button_recenter)
        completeRouteButton = view.findViewById(R.id.button_complete_route)

        findRoutesButton.setOnClickListener { onFindRoutesClicked() }
        recenterButton.setOnClickListener { recenterOnUser() }
        completeRouteButton.setOnClickListener { onCompleteRouteClicked() }
    }

    // ------------------------------------------------------------
    // Map callbacks
    // ------------------------------------------------------------

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.uiSettings?.isZoomControlsEnabled = true
        map?.uiSettings?.isMyLocationButtonEnabled = false // we have our own button

        enableMyLocationIfPermitted()
    }

    private fun enableMyLocationIfPermitted() {
        val ctx = context ?: return

        val fineGranted = ActivityCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ActivityCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            map?.isMyLocationEnabled = true
            getLastKnownLocation()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults.any { it == PackageManager.PERMISSION_GRANTED }
            ) {
                enableMyLocationIfPermitted()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission is required to show routes",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ------------------------------------------------------------
    // Location
    // ------------------------------------------------------------

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    private fun getLastKnownLocation() {
        val act = activity ?: return

        fusedLocationClient
            .lastLocation   // property returning Task<Location>
            .addOnSuccessListener(act) { loc: Location? ->
                if (loc != null) {
                    val userLatLng = LatLng(loc.latitude, loc.longitude)
                    currentLocation = userLatLng
                    Log.d(TAG, "Got real location: $currentLocation")

                    map?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            userLatLng,
                            DEFAULT_ZOOM
                        )
                    )
                } else {
                    Log.d(TAG, "lastLocation was null")
                }
            }
            .addOnFailureListener(act) { e ->
                Log.e(TAG, "Error getting location", e)
            }
    }

    private fun recenterOnUser() {
        val user = currentLocation
        if (user != null) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    user,
                    DEFAULT_ZOOM
                )
            )
        } else {
            Toast.makeText(requireContext(), "Location not ready yet", Toast.LENGTH_SHORT).show()
            enableMyLocationIfPermitted()
        }
    }

    // ------------------------------------------------------------
    // Routes / loop
    // ------------------------------------------------------------

    private fun onFindRoutesClicked() {
        val distanceText = distanceInput.text.toString().trim()
        if (distanceText.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a distance in km", Toast.LENGTH_SHORT).show()
            return
        }

        val distanceKm = distanceText.toDoubleOrNull()
        if (distanceKm == null || distanceKm <= 0.0) {
            Toast.makeText(requireContext(), "Invalid distance", Toast.LENGTH_SHORT).show()
            return
        }

        val center = currentLocation
        if (center == null) {
            Toast.makeText(requireContext(), "Waiting for location…", Toast.LENGTH_SHORT).show()
            enableMyLocationIfPermitted()
            return
        }

        // Build and draw the loop
        val loopPoints = buildLoopAround(center, distanceKm)
        drawLoop(loopPoints)

        // Remember this route distance so we can award points later
        lastRouteDistanceKm = distanceKm
        completeRouteButton.visibility = View.VISIBLE

        // Also launch it in Google Maps for navigation
        openInGoogleMaps(loopPoints)
    }

    /**
     * Make a simple 4-point loop (square-ish) around the user where total length ≈ distanceKm.
     */
    private fun buildLoopAround(center: LatLng, distanceKm: Double): List<LatLng> {
        // very rough: 1 degree latitude ~ 111 km
        val radiusKm = distanceKm / (2 * Math.PI) // circumference ≈ distance
        val deltaDeg = radiusKm / 111.0

        val angles = listOf(0.0, Math.PI / 2, Math.PI, 3 * Math.PI / 2)
        val points = angles.map { a ->
            val dx = deltaDeg * cos(a)
            val dy = deltaDeg * sin(a)
            LatLng(center.latitude + dy, center.longitude + dx)
        }

        return points + points.first() // close loop
    }

    private fun drawLoop(points: List<LatLng>) {
        loopPolyline?.remove()
        loopPolyline = map?.addPolyline(
            PolylineOptions()
                .addAll(points)
                .color(0xFF6200EE.toInt()) // purple_700-ish
                .width(8f)
        )

        // Zoom to fit the loop
        if (points.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            points.forEach { builder.include(it) }
            val bounds = builder.build()
            val padding = 120
            map?.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, padding)
            )
        }
    }

    /**
     * Open the same loop in the Google Maps app so the user can navigate it.
     */
    private fun openInGoogleMaps(points: List<LatLng>) {
        if (points.size < 2) return

        val origin = points.first()
        val dest = points.last()

        // Use middle points as waypoints
        val waypoints = points.drop(1).dropLast(1)

        val wpParam = if (waypoints.isNotEmpty()) {
            "waypoints=" + waypoints.joinToString("|") { "${it.latitude},${it.longitude}" } + "&"
        } else {
            ""
        }

        val uri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1" +
                    "&origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${dest.latitude},${dest.longitude}" +
                    "&${wpParam}travelmode=walking"
        )

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    // ------------------------------------------------------------
    // Completing a route & awarding points
    // ------------------------------------------------------------

    private fun onCompleteRouteClicked() {
        val distanceKm = lastRouteDistanceKm
        if (distanceKm == null) {
            Toast.makeText(requireContext(), "No active route to complete", Toast.LENGTH_SHORT)
                .show()
            completeRouteButton.visibility = View.GONE
            return
        }

        // Simple scoring: distance * POINTS_PER_KM
        val points = (distanceKm * POINTS_PER_KM).toInt().coerceAtLeast(1)
        awardPoints(points)

        Toast.makeText(
            requireContext(),
            "Nice job! You earned $points points 🎉",
            Toast.LENGTH_LONG
        ).show()

        // Clear state
        lastRouteDistanceKm = null
        completeRouteButton.visibility = View.GONE
    }

    private fun awardPoints(points: Int) {
        val user = auth.currentUser
        if (user == null) {
            Log.w(TAG, "awardPoints: no logged-in user")
            return
        }

        val uid = user.uid

        // Increment the user's total points field
        val userRef = db.collection(USER_COLLECTION).document(uid)
        val update = mapOf(
            POINTS_FIELD to FieldValue.increment(points.toLong())
        )

        userRef.set(update, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Successfully added $points points for user $uid")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to award points", e)
            }
    }
}
