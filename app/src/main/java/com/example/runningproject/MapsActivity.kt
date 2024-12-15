package com.example.runningproject

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.runningproject.databinding.ActivityMapsBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.android.gms.maps.model.PolylineOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationCallback: LocationCallback
    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth

    private var previousLocation: LatLng? = null
    private var startLocation: LatLng? = null

    private var totalDistance = 0.0
    private var startTime: Long = 0
    private var totalCalories = 0.0
    private var uniqueRouteId: String? = null

    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
        return results[0].toDouble()
    }

    private fun calculateCalories(distance: Double): Double {
        val weight = 60 // Berat pengguna dalam kg, bisa disesuaikan atau diambil dari input pengguna
        val caloriesPerKm = 0.75 * weight
        return (distance / 1000) * caloriesPerKm
    }

    private fun calculatePace(distance: Double, time: Long): Double {
        val timeInMinutes = time / 60000.0
        return timeInMinutes / (distance / 1000)
    }

    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private fun addPolylineAtLocation(startLocation: LatLng, endLocation: LatLng) {
        mMap.addPolyline(
            PolylineOptions()
                .add(startLocation, endLocation)
                .width(5f) // lebar garis
                .color(Color.RED)
        )
    }

    private fun saveLocationToFirestore(location: LatLng) {
        val userId = auth.currentUser?.uid ?: return
        val locationData = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "timestamp" to System.currentTimeMillis()
        )
        val routesDocRef = db.collection("users").document(userId).collection("routes").document(uniqueRouteId!!)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        routesDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Update totalDistance, totalCalories, and pace before saving
                val currentTime = System.currentTimeMillis()
                val pace = if (totalDistance > 0) calculatePace(totalDistance, currentTime - startTime) else 0.0

                routesDocRef.update(
                    "locations", FieldValue.arrayUnion(locationData),
                    "totalDistance", totalDistance,
                    "totalCalories", totalCalories,
                    "pace", pace,
                    "date", currentDate
                ).addOnSuccessListener {
                    Log.d("MapsActivity", "Location and metrics added to route array successfully")
                }.addOnFailureListener { e ->
                    Log.w("MapsActivity", "Error adding location and metrics to route array", e)
                }
            } else {
                val pace = if (totalDistance > 0) calculatePace(totalDistance, System.currentTimeMillis() - startTime) else 0.0
                val routeData = hashMapOf(
                    "locations" to arrayListOf(locationData),
                    "totalDistance" to totalDistance,
                    "totalCalories" to totalCalories,
                    "pace" to pace,
                    "date" to currentDate
                )
                routesDocRef.set(routeData).addOnSuccessListener {
                    Log.d("MapsActivity", "Route document created with initial location array and metrics")
                }.addOnFailureListener { e ->
                    Log.w("MapsActivity", "Error creating route document with metrics", e)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    startLocationUpdates()
                } else {
                    showPermissionRationale {
                        requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                    }
                }
            }

        binding.backButton.setOnClickListener {
            val intent = Intent(this, HomeFragment::class.java)
            startActivity(intent)
        }

        binding.startButton.setOnClickListener {
            if (hasLocationPermission()) {
                uniqueRouteId = UUID.randomUUID().toString() // Generate a new unique ID
                startLocationUpdates()
            } else {
                requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }
        }

        binding.stopButton.setOnClickListener {
            stopLocationUpdates()
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    updateMapLocation(userLocation)
                    uniqueRouteId?.let { saveLocationToFirestore(userLocation) }

                    if (startLocation == null) {
                        startLocation = userLocation
                        startTime = System.currentTimeMillis()
                        addMarkerAtLocation(userLocation, "Start")
                    } else {
                        previousLocation?.let {
                            val distance = calculateDistance(it, userLocation)
                            totalDistance += distance
                            totalCalories = calculateCalories(totalDistance)
                            val currentTime = System.currentTimeMillis()
                            val pace = if (totalDistance > 0) calculatePace(totalDistance, currentTime - startTime) else 0.0

                            // Update UI with distance, calories, and pace
                            val formattedDistance = if (totalDistance >= 100) {
                                String.format("%06.2f m", totalDistance)
                            } else {
                                String.format("%05.2f m", totalDistance)
                            }

                            val formattedCalories = if (totalCalories >= 100) {
                                String.format("%06.2f kcal", totalCalories)
                            } else {
                                String.format("%05.2f kcal", totalCalories)
                            }

                            val formattedPace = if (pace >= 100) {
                                String.format("%06.2f min/km", pace)
                            } else {
                                String.format("%05.2f min/km", pace)
                            }

                            binding.distanceTextView.text = formattedDistance
                            binding.caloriesTextView.text = formattedCalories
                            binding.paceTextView.text = formattedPace

                            addPolylineAtLocation(it, userLocation)
                        }
                    }
                    previousLocation = userLocation
                }
            }
        }
    }

    private fun showPermissionRationale(positiveAction: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Location permission")
            .setMessage("This app will not work without knowing your current location")
            .setPositiveButton(android.R.string.ok) { _, _ -> positiveAction() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create().show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (hasLocationPermission()) {
            startLocationUpdates()
        } else {
            requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    private fun startLocationUpdates() {
        if (hasLocationPermission()) {
            val locationRequest = LocationRequest.create().apply {
                interval = 5000
                fastestInterval = 3000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            try {
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            } catch (e: SecurityException) {
                Log.e("MapsActivity", "Location permission not granted", e)
            }
        } else {
            requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        previousLocation?.let {
            addMarkerAtLocation(it, "End")
        }
    }

    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun addMarkerAtLocation(location: LatLng, title: String) {
        mMap.addMarker(MarkerOptions().title(title).position(location))
    }
}