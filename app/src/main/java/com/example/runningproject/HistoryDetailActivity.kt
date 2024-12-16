package com.example.runningproject

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class HistoryDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var backButton: ImageButton
    private lateinit var historyDetailDate: TextView
    private lateinit var historyDetailDistance: TextView
    private lateinit var historyDetailCalories: TextView
    private lateinit var historyDetailPace: TextView
    private var locations: List<LatLng> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_detail)

        // Initialize the views
        mapView = findViewById(R.id.historyDetailMapView)
        backButton = findViewById(R.id.backButton)
        historyDetailDate = findViewById(R.id.historyDetailDate)
        historyDetailDistance = findViewById(R.id.historyDetailDistance)
        historyDetailCalories = findViewById(R.id.historyDetailCalories)
        historyDetailPace = findViewById(R.id.historyDetailPace)

        // Initialize the MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Set up the back button
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Get the history item from the intent
        val historyItem: HistoryItem? = intent.getParcelableExtra("HISTORY_ITEM")

        // Display the history item details
        historyItem?.let {
            historyDetailDate.text = it.date
            historyDetailDistance.text = "Distance: ${it.distance}"
            historyDetailCalories.text = "Calories: ${it.calories}"
            historyDetailPace.text = "Pace: ${it.pace}"
            locations = it.locations
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (locations.isNotEmpty()) {
            val polylineOptions = PolylineOptions().addAll(locations).width(5f).color(android.graphics.Color.RED)
            googleMap.addPolyline(polylineOptions)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locations[0], 15f))
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}