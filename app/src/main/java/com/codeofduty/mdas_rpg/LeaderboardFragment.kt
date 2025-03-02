package com.codeofduty.mdas_rpg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class LeaderboardFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardAdapter
    private var leaderboardItems: MutableList<LeaderboardItemFIrebase> = mutableListOf()
    private lateinit var animEmpty: View
    private lateinit var tvEmpty: TextView

    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_leaderboard)
        recyclerView.layoutManager = LinearLayoutManager(context)

        animEmpty = view.findViewById(R.id.anim_empty)
        tvEmpty = view.findViewById(R.id.tv_empty)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("game_table")

        // Fetch data from Firebase
        fetchLeaderboardData()

        return view
    }

    private fun fetchLeaderboardData() {
        // Attach a listener to Firebase Realtime Database
        databaseReference.orderByChild("score").limitToLast(10).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                leaderboardItems.clear()

                for (dataSnapshot in snapshot.children) {
                    val leaderboardItem = dataSnapshot.getValue(LeaderboardItemFIrebase::class.java)
                    leaderboardItem?.let {
                        leaderboardItems.add(it)
                    }
                }

                // Check if leaderboard is empty
                if (leaderboardItems.isEmpty()) {
                    animEmpty.visibility = View.VISIBLE
                    tvEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    animEmpty.visibility = View.GONE
                    tvEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }

                // Update the RecyclerView adapter
                adapter = LeaderboardAdapter(leaderboardItems)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors that may occur
            }
        })
    }
}
