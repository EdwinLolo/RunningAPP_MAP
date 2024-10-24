package com.example.runningproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Create sample data
        val historyList = listOf(
            HistoryItem("November 26", "10,12 km", "701 kcal   11,2 km/hr"),
            HistoryItem("November 21", "9,89 km", "669 kcal   10,8 km/hr"),
            HistoryItem("November 16", "9,12 km", "608 kcal   10 km/hr")
        )

        // Set the adapter
        historyAdapter = HistoryAdapter(historyList)
        recyclerView.adapter = historyAdapter

        return view
    }
}
