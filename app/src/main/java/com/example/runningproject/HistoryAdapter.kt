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
        val mapPreview: ImageView = itemView.findViewById(R.id.mapPreview)
        val historyDate: TextView = itemView.findViewById(R.id.historyDate)
        val historyDistance: TextView = itemView.findViewById(R.id.historyDistance)
        val historyStats: TextView = itemView.findViewById(R.id.historyStats)
        val arrowIcon: ImageView = itemView.findViewById(R.id.arrowIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]
        holder.historyDate.text = history.date
        holder.historyDistance.text = history.distance
        holder.historyStats.text = history.stats
        // You can set map preview and arrow icon here if needed
    }

    override fun getItemCount(): Int {
        return historyList.size
    }
}
