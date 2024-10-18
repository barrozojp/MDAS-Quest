package com.codeofduty.mdas_rpg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(private val leaderboardItems: List<LeaderboardItem>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val difficulty: TextView = itemView.findViewById(R.id.operationdifficulty)
        val totalScore: TextView = itemView.findViewById(R.id.total_score)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val item = leaderboardItems[position]
        holder.name.text = item.name
        holder.difficulty.text = item.difficulty
        holder.totalScore.text = item.totalScore
    }

    override fun getItemCount(): Int {
        return leaderboardItems.size
    }
}
