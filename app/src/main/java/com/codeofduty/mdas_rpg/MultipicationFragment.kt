package com.codeofduty.mdas_rpg

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


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
            // Start the EasyMultiplication activity
            val intent = Intent(requireContext(), EasyMultiplication::class.java)
            startActivity(intent)
        }

        return view
    }
}