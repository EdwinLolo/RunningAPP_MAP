package com.example.runningproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class HistoryAdapter(private val historyList: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OnMapReadyCallback {
        val dateTextView: TextView = itemView.findViewById(R.id.historyDate)
        val distanceTextView: TextView = itemView.findViewById(R.id.historyDistance)
        val caloriesTextView: TextView = itemView.findViewById(R.id.historyCalories)
        val paceTextView: TextView = itemView.findViewById(R.id.historyPace)
        val mapView: MapView = itemView.findViewById(R.id.historyMapView)
        var googleMap: GoogleMap? = null
        var locations: List<LatLng> = emptyList()

        init {
            mapView.onCreate(null)
            mapView.getMapAsync(this)
        }

        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            googleMap?.uiSettings?.setAllGesturesEnabled(false)
            if (locations.isNotEmpty()) {
                val polylineOptions = PolylineOptions().addAll(locations).width(5f).color(android.graphics.Color.RED)
                googleMap?.addPolyline(polylineOptions)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(locations[0], 15f))
            }
        }

        fun bind(historyItem: HistoryItem) {
            dateTextView.text = historyItem.date
            distanceTextView.text = historyItem.distance
            caloriesTextView.text = historyItem.calories
            paceTextView.text = historyItem.pace
            locations = historyItem.locations
            mapView.getMapAsync(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = historyList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = historyList.size

    override fun onViewRecycled(holder: HistoryViewHolder) {
        super.onViewRecycled(holder)
        holder.mapView.onDestroy()
    }
}