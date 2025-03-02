package com.codeofduty.mdas_rpg

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MultipicationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_multipication, container, false)

        // Set up the button click to launch EasyMultiplication activity
        val btnEasy = view.findViewById<Button>(R.id.btn_easy)
        btnEasy.setOnClickListener {
            // Get the logged-in username from SharedPreferences
            val game_username = requireActivity().getSharedPreferences("MyAppPrefs", AppCompatActivity.MODE_PRIVATE)
                .getString("username", "") ?: ""  // Default to empty if not found

            // Show a toast with the game username to verify it's being retrieved
            Toast.makeText(requireContext(), "Goodluck: $game_username !", Toast.LENGTH_SHORT).show()
            // Start the EasyMultiplication activity
            val intent = Intent(requireContext(), EasyMultiplication::class.java)
            intent.putExtra("operation_difficulty", "Multiply/Easy")  // Pass difficulty level
            intent.putExtra("game_username", game_username)  // Pass the logged-in username

            startActivity(intent)
        }

        // Set up the button click to launch MEDIUM
        val btnMedium = view.findViewById<Button>(R.id.btn_medium)
        btnMedium.setOnClickListener {
            // Get the logged-in username from SharedPreferences
            val game_username = requireActivity().getSharedPreferences("MyAppPrefs", AppCompatActivity.MODE_PRIVATE)
                .getString("username", "") ?: ""  // Default to empty if not found

            // Show a toast with the game username to verify it's being retrieved
            Toast.makeText(requireContext(), "Goodluck: $game_username !", Toast.LENGTH_SHORT).show()
            // Start the Medium activity
            val intent = Intent(requireContext(), MediumMultiplication::class.java)
            intent.putExtra("operation_difficulty", "Multiply/Medium")  // Pass difficulty level
            Toast.makeText(requireContext(), "Goodluck: $game_username !", Toast.LENGTH_SHORT).show()

            startActivity(intent)
        }

        // Set up the button click to launch MEDIUM
        val btnHard = view.findViewById<Button>(R.id.btn_hard)
        btnHard.setOnClickListener {
            // Get the logged-in username from SharedPreferences
            val game_username = requireActivity().getSharedPreferences("MyAppPrefs", AppCompatActivity.MODE_PRIVATE)
                .getString("username", "") ?: ""  // Default to empty if not found

            // Show a toast with the game username to verify it's being retrieved
            Toast.makeText(requireContext(), "Goodluck: $game_username !", Toast.LENGTH_SHORT).show()
            // Start the Hard activity
            val intent = Intent(requireContext(), HardMultiplication::class.java)
            intent.putExtra("operation_difficulty", "Multiply/Hard")  // Pass difficulty level
            Toast.makeText(requireContext(), "Goodluck: $game_username !", Toast.LENGTH_SHORT).show()

            startActivity(intent)
        }

        return view
    }
}