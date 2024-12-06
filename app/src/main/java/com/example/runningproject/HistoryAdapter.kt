package com.example.runningproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val historyList: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.historyDate)
        val distanceTextView: TextView = itemView.findViewById(R.id.historyDistance)
        val caloriesTextView: TextView = itemView.findViewById(R.id.historyCalories)
        val paceTextView: TextView = itemView.findViewById(R.id.historyPace)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = historyList[position]
        holder.dateTextView.text = currentItem.date

        val distance = currentItem.distance.replace(" km", "").toDouble()
        val calories = currentItem.calories.replace(" kcal", "").toDouble()
        val pace = currentItem.pace.replace(" min/km", "").toDouble()

        holder.distanceTextView.text = String.format("%.2f km", distance)
        holder.caloriesTextView.text = String.format("%.1f kcal", calories)
        holder.paceTextView.text = String.format("%.2f min/km", pace)
    }

    override fun getItemCount() = historyList.size
}
