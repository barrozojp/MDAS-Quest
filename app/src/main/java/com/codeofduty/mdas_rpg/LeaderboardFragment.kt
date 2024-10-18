package com.codeofduty.mdas_rpg

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class LeaderboardFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardAdapter
    private lateinit var leaderboardItems: List<LeaderboardItem>
    private lateinit var animEmpty: View
    private lateinit var tvEmpty: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_leaderboard)
        recyclerView.layoutManager = LinearLayoutManager(context)

        animEmpty = view.findViewById(R.id.anim_empty)
        tvEmpty = view.findViewById(R.id.tv_empty)

        // Fetch leaderboard data from the database without filtering by username
        val databaseHelper = DatabaseHelper(requireContext())
        leaderboardItems = databaseHelper.getLeaderboard() // No username filtering

        adapter = LeaderboardAdapter(leaderboardItems)
        recyclerView.adapter = adapter

        if (leaderboardItems.isEmpty()) {
            animEmpty.visibility = View.VISIBLE
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            animEmpty.visibility = View.GONE
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }

        return view
    }
}

