package com.codeofduty.mdas_rpg

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


class HomeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Find the multiplication button
        val multiplicationBtn = view.findViewById<Button>(R.id.btn_multiplication)

        // Set OnClickListener for the multiplication button
        multiplicationBtn.setOnClickListener {
            // Navigate to the MultiplicationFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MultipicationFragment())
                .addToBackStack(null)  // Adds the transaction to the back stack
                .commit()
        }

        return view
    }

}