package com.codeofduty.mdas_rpg

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


class AdditionFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_addition, container, false)

        // EASY
        val btnEasy = view.findViewById<Button>(R.id.btn_easy)
        btnEasy.setOnClickListener {
            val intent = Intent(requireContext(), EasyAddition::class.java)
            intent.putExtra("operation_difficulty", "Add/Easy")  // Pass difficulty level
            startActivity(intent)
        }

        // MEDIUM
        val btnMedium = view.findViewById<Button>(R.id.btn_medium)
        btnMedium.setOnClickListener {
            val intent = Intent(requireContext(), MediumAddition::class.java)
            intent.putExtra("operation_difficulty", "Add/Medium")  // Pass difficulty level
            startActivity(intent)
        }

        // HARD
        val btnHard = view.findViewById<Button>(R.id.btn_hard)
        btnHard.setOnClickListener {
            val intent = Intent(requireContext(), HardAddition::class.java)
            intent.putExtra("operation_difficulty", "Add/Hard")  // Pass difficulty level
            startActivity(intent)
        }

        return view
    }
}
