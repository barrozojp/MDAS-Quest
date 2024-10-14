package com.codeofduty.mdas_rpg

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


class DivisionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_division, container, false)

        // Set up the button click to launch EASY activity
        val btnEasy = view.findViewById<Button>(R.id.btn_easy)
        btnEasy.setOnClickListener {
            // Start the EasyMultiplication activity
            val intent = Intent(requireContext(), EasyDivision::class.java)
            intent.putExtra("operation_difficulty", "Divide/Easy")  // Pass difficulty level
            startActivity(intent)
        }

        // Set up the button click to launch MEDIUM
        val btnMedium = view.findViewById<Button>(R.id.btn_medium)
        btnMedium.setOnClickListener {
            // Start the Medium activity
            val intent = Intent(requireContext(), MediumDivision::class.java)
            intent.putExtra("operation_difficulty", "Divide/Medium")  // Pass difficulty level
            startActivity(intent)
        }

        // Set up the button click to launch MEDIUM
        val btnHard = view.findViewById<Button>(R.id.btn_hard)
        btnHard.setOnClickListener {
            // Start the Hard activity
            val intent = Intent(requireContext(), HardDivision::class.java)
            intent.putExtra("operation_difficulty", "Divide/Hard")  // Pass difficulty level
            startActivity(intent)
        }

        return view
    }

}