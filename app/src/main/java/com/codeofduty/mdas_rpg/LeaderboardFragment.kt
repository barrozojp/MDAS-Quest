package com.codeofduty.mdas_rpg

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class LeaderboardFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardAdapter
    private lateinit var leaderboardItems: List<LeaderboardItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_leaderboard)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Sample data, replace with your actual data
        leaderboardItems = listOf(
            LeaderboardItem("1st", "???", "???"),
            LeaderboardItem("2nd", "???", "???"),
            LeaderboardItem("3rd", "???", "???"),
            LeaderboardItem("4th", "???", "???"),
            LeaderboardItem("5th", "???", "???"),
            LeaderboardItem("6th", "???", "???"),
            LeaderboardItem("7th", "???", "???"),
            LeaderboardItem("8th", "???", "???"),
            LeaderboardItem("9th", "???", "???"),
            LeaderboardItem("10th", "???", "???")

            )

        adapter = LeaderboardAdapter(leaderboardItems)
        recyclerView.adapter = adapter

        return view
    }
}
