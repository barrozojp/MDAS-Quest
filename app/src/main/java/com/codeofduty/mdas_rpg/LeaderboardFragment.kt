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
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var animEmpty: View
    private lateinit var tvEmpty: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_leaderboard)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the anim_empty and tvEmpty views
        animEmpty = view.findViewById(R.id.anim_empty)
        tvEmpty = view.findViewById(R.id.tv_empty)


        // Retrieve username from SharedPreferences
        sharedPreferences =
            requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
            ?: "" // Ensure it returns empty string if null

        // Fetch leaderboard data from the database using username
        val databaseHelper = DatabaseHelper(requireContext())
        leaderboardItems =
            databaseHelper.getLeaderboard(username) // Pass the username to fetch actual data

        adapter = LeaderboardAdapter(leaderboardItems)
        recyclerView.adapter = adapter

        // Check if the leaderboard is empty and toggle visibility of anim_empty and tvEmpty
        if (leaderboardItems.isEmpty()) {
            animEmpty.visibility = View.VISIBLE
            tvEmpty.visibility = View.VISIBLE // Show the TextView
            recyclerView.visibility = View.GONE // Hide the RecyclerView
        } else {
            animEmpty.visibility = View.GONE
            tvEmpty.visibility = View.GONE // Hide the TextView
            recyclerView.visibility = View.VISIBLE // Show the RecyclerView
        }

        return view
    }
}

