package com.example.runningproject

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.android.gms.maps.model.LatLng

@Parcelize
data class HistoryItem(
    val date: String,
    val distance: String,
    val calories: String,
    val pace: String,
    val locations: List<LatLng>
) : Parcelable