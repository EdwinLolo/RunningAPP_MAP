package com.example.runningproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommunityAdapter(private val communityList: List<String>) :
    RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    class CommunityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val communityName: TextView = view.findViewById(R.id.community_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community, parent, false)
        return CommunityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        holder.communityName.text = communityList[position]
    }

    override fun getItemCount() = communityList.size
}
