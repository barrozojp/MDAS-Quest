package com.codeofduty.mdas_rpg

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


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
            // Start the Easy activity
            val intent = Intent(requireContext(), EasySubtraction::class.java)
            intent.putExtra("operation_difficulty", "Minus/Easy")  // Pass difficulty level
            startActivity(intent)
        }

        // MEDIUM
        val btnMedium = view.findViewById<Button>(R.id.btn_medium)
        btnMedium.setOnClickListener {
            // Start the Medium activity
            val intent = Intent(requireContext(), MediumSubtraction::class.java)
            intent.putExtra("operation_difficulty", "Minus/Medium")  // Pass difficulty level
            startActivity(intent)
        }

        //HARD
        val btnHard = view.findViewById<Button>(R.id.btn_hard)
        btnHard.setOnClickListener {
            // Start the Hard activity
            val intent = Intent(requireContext(), HardSubtraction::class.java)
            intent.putExtra("operation_difficulty", "Minus/Hard")  // Pass difficulty level
            startActivity(intent)

        }

        return view
    }

}