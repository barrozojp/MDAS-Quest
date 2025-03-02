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


class SubtractionFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_subtraction, container, false)

        // EASY
        val btnEasy = view.findViewById<Button>(R.id.btn_easy)
        btnEasy.setOnClickListener {
            // Get the logged-in username from SharedPreferences
            val game_username = requireActivity().getSharedPreferences("MyAppPrefs", AppCompatActivity.MODE_PRIVATE)
                .getString("username", "") ?: ""  // Default to empty if not found

            // Show a toast with the game username to verify it's being retrieved
            Toast.makeText(requireContext(), "Goodluck: $game_username !", Toast.LENGTH_SHORT).show()
            // Start the Easy activity
            val intent = Intent(requireContext(), EasySubtraction::class.java)
            intent.putExtra("operation_difficulty", "Minus/Easy")  // Pass difficulty level
            intent.putExtra("game_username", game_username)  // Pass the logged-in username

            startActivity(intent)
        }

        // MEDIUM
        val btnMedium = view.findViewById<Button>(R.id.btn_medium)
        btnMedium.setOnClickListener {
            // Get the logged-in username from SharedPreferences
            val game_username = requireActivity().getSharedPreferences("MyAppPrefs", AppCompatActivity.MODE_PRIVATE)
                .getString("username", "") ?: ""  // Default to empty if not found

            // Show a toast with the game username to verify it's being retrieved
            Toast.makeText(requireContext(), "Goodluck: $game_username !", Toast.LENGTH_SHORT).show()
            // Start the Medium activity
            val intent = Intent(requireContext(), MediumSubtraction::class.java)
            intent.putExtra("operation_difficulty", "Minus/Medium")  // Pass difficulty level
            intent.putExtra("game_username", game_username)  // Pass the logged-in username

            startActivity(intent)
        }

        //HARD
        val btnHard = view.findViewById<Button>(R.id.btn_hard)
        btnHard.setOnClickListener {
            // Get the logged-in username from SharedPreferences
            val game_username = requireActivity().getSharedPreferences("MyAppPrefs", AppCompatActivity.MODE_PRIVATE)
                .getString("username", "") ?: ""  // Default to empty if not found

            // Show a toast with the game username to verify it's being retrieved
            Toast.makeText(requireContext(), "Goodluck: $game_username !", Toast.LENGTH_SHORT).show()
            // Start the Hard activity
            val intent = Intent(requireContext(), HardSubtraction::class.java)
            intent.putExtra("operation_difficulty", "Minus/Hard")  // Pass difficulty level
            intent.putExtra("game_username", game_username)  // Pass the logged-in username

            startActivity(intent)

        }

        return view
    }

}