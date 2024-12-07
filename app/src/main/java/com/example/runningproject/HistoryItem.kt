package com.example.runningproject
import com.google.android.gms.maps.model.LatLng

data class HistoryItem(
    val date: String,
    val distance: String,
    val calories: String,
    val pace: String,
    val locations: List<LatLng>
)
