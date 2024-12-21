package com.example.runningproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runningproject.HistoryAdapter
import com.example.runningproject.HistoryItem
import com.example.runningproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.android.gms.maps.model.LatLng

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Bind the TextViews for username and user level
        val userNameTextView = view.findViewById<TextView>(R.id.user_name)
        val userLevelTextView = view.findViewById<TextView>(R.id.user_level)

        // Observe the SharedViewModel and update username and level
        sharedViewModel.username.observe(viewLifecycleOwner, { username ->
            userNameTextView.text = username
        })

        // Fetch history data from Firestore
        fetchHistoryData()

        return view
    }

    private fun fetchHistoryData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(userId).collection("routes")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val historyList = mutableListOf<HistoryItem>()
                var totalDistance = 0.0
                var totalCalories = 0.0
                var totalPace = 0.0
                var paceCount = 0

                for (document in documents) {
                    val date = document.get("date")?.let {
                        when (it) {
                            is String -> it
                            is com.google.firebase.Timestamp -> {
                                val timestamp = it
                                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                                dateFormat.format(timestamp.toDate())
                            }
                            else -> ""
                        }
                    } ?: ""

                    val distance = document.getDouble("totalDistance") ?: 0.0
                    val calories = document.getDouble("totalCalories") ?: 0.0
                    val pace = document.getDouble("pace") ?: 0.0

                    totalDistance += distance
                    totalCalories += calories
                    if (pace > 0) {
                        totalPace += pace
                        paceCount++
                    }

                    val locations = document.get("locations") as? List<Map<String, Any>> ?: emptyList()
                    val locationList = locations.map { loc ->
                        LatLng(loc["latitude"] as Double, loc["longitude"] as Double)
                    }

                    historyList.add(HistoryItem(date, String.format("%.2f m", distance), String.format("%.0f kcal", calories), String.format("%.1f min/km", pace), locationList))
                }

                // Update adapter with the latest data
                historyAdapter = HistoryAdapter(historyList)
                recyclerView.adapter = historyAdapter

                // Update the total TextViews
                view?.findViewById<TextView>(R.id.total_distance)?.text = String.format("%.2f km", totalDistance / 1000)
                view?.findViewById<TextView>(R.id.total_calories)?.text = String.format("%.0f kcal", totalCalories)
                view?.findViewById<TextView>(R.id.total_pace)?.text = if (paceCount > 0) String.format("%.1f min/km", totalPace / paceCount) else "0.0 min/km"
            }
            .addOnFailureListener { exception ->
                Log.w("HistoryFragment", "Error getting documents: ", exception)
            }
    }
    override fun onResume() {
        super.onResume()
        recyclerView.adapter?.let { adapter ->
            for (i in 0 until adapter.itemCount) {
                val holder = recyclerView.findViewHolderForAdapterPosition(i) as? HistoryAdapter.HistoryViewHolder
                holder?.mapView?.onResume()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        recyclerView.adapter?.let { adapter ->
            for (i in 0 until adapter.itemCount) {
                val holder = recyclerView.findViewHolderForAdapterPosition(i) as? HistoryAdapter.HistoryViewHolder
                holder?.mapView?.onPause()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerView.adapter?.let { adapter ->
            for (i in 0 until adapter.itemCount) {
                val holder = recyclerView.findViewHolderForAdapterPosition(i) as? HistoryAdapter.HistoryViewHolder
                holder?.mapView?.onDestroy()
            }
        }
    }
}