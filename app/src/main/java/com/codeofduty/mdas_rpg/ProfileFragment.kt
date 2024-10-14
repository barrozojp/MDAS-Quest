package com.codeofduty.mdas_rpg

import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


class ProfileFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var usernameTextView: TextView // Reference to your TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize the TextView for displaying the username
        usernameTextView = view.findViewById(R.id.hi_userTV) // Ensure this ID matches your layout

        sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        // Retrieve and set the username from SharedPreferences
        val username = sharedPreferences.getString("username", "User") // Default to "User" if not found
        usernameTextView.text = "Hi $username!" // Set the username in the TextView

        // Set up the logout button click listener
        val logoutButton: TextView = view.findViewById(R.id.btn_logout) // Ensure this ID matches your layout
        logoutButton.setOnClickListener {
            showLogoutDialog()
        }

        return view
    }

    private fun showLogoutDialog() {
        // Create a Dialog
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_logout) // Ensure this matches your layout file
        dialog.setCancelable(true)

        // Initialize buttons
        val yesButton: TextView = dialog.findViewById(R.id.logout_yes)
        val noButton: TextView = dialog.findViewById(R.id.logout_no)

        // Yes button click listener
        yesButton.setOnClickListener {
            // Clear SharedPreferences
            sharedPreferences.edit().clear().apply()

            // Navigate back to the LoginActivity
            val intent = Intent(requireActivity(), LoginRegister::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the activity stack
            startActivity(intent)
            dialog.dismiss() // Dismiss the dialog
        }

        // No button click listener
        noButton.setOnClickListener {
            dialog.dismiss() // Just dismiss the dialog
        }

        // Show the dialog
        dialog.show()
    }
}