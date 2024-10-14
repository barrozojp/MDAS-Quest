package com.codeofduty.mdas_rpg

import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import android.widget.Toast

class ProfileFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var usernameTextView: TextView
    private lateinit var loadingDialog: Dialog
    private lateinit var dbHelper: DatabaseHelper // Add this to handle database operations

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize the TextView for displaying the username
        usernameTextView = view.findViewById(R.id.hi_userTV)

        // Set up loading dialog
        setupLoadingDialog()

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper(requireContext())

        sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        // Retrieve and set the username from SharedPreferences
        val username = sharedPreferences.getString("username", "User")
        usernameTextView.text = "Hi $username!"

        // Set up the logout button click listener
        val logoutButton: TextView = view.findViewById(R.id.btn_logout)
        logoutButton.setOnClickListener {
            showLogoutDialog()
        }

        // Set up the Change Username button click listener
        val changeUsernameButton: AppCompatButton = view.findViewById(R.id.btn_changeUsername)
        changeUsernameButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChangeUsernameFragment())
                .addToBackStack(null)
                .commit()
        }

        // Set up the Change Password button click listener
        val changePasswordButton: AppCompatButton = view.findViewById(R.id.btn_changePassword)
        changePasswordButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChangePasswordFragment())
                .addToBackStack(null)
                .commit()
        }

        // Set up the Delete Account button click listener
        val deleteAccountButton: AppCompatButton = view.findViewById(R.id.btn_deleteAcc)
        deleteAccountButton.setOnClickListener {
            showDeleteAccountDialog()
        }

        return view
    }

    private fun showDeleteAccountDialog() {
        // Create a Dialog
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_account_deletion) // Use your deletion dialog layout
        dialog.setCancelable(true)

        // Initialize buttons
        val yesButton: TextView = dialog.findViewById(R.id.delete_yes)
        val noButton: TextView = dialog.findViewById(R.id.delete_no)

        // Yes button click listener
        yesButton.setOnClickListener {
            loadingDialog.show()

            // Delay to simulate account deletion process
            Handler(Looper.getMainLooper()).postDelayed({
                // Get the current username
                val username = sharedPreferences.getString("username", "") ?: ""

                // Delete the user account from the database
                val deleteSuccess = dbHelper.deleteUserAccount(username)

                // Clear SharedPreferences
                sharedPreferences.edit().clear().apply()

                // Handle the result of the deletion
                if (deleteSuccess) {
                    Toast.makeText(requireContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show()

                    // Navigate back to the LoginRegister activity
                    val intent = Intent(requireActivity(), LoginRegister::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Failed to delete account.", Toast.LENGTH_SHORT).show()
                }

                // Dismiss loading dialog and deletion dialog
                dialog.dismiss()
                loadingDialog.dismiss()
            }, 2000) // 2-second delay for simulation
        }

        // No button click listener
        noButton.setOnClickListener {
            dialog.dismiss() // Just dismiss the dialog
        }

        // Show the dialog
        dialog.show()
    }

    private fun showLogoutDialog() {
        // Create a Dialog
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_logout)
        dialog.setCancelable(true)

        // Initialize buttons
        val yesButton: TextView = dialog.findViewById(R.id.logout_yes)
        val noButton: TextView = dialog.findViewById(R.id.logout_no)

        // Yes button click listener
        yesButton.setOnClickListener {
            // Show loading dialog
            loadingDialog.show()

            // Delay to simulate logout process
            Handler(Looper.getMainLooper()).postDelayed({
                // Clear SharedPreferences
                sharedPreferences.edit().clear().apply()

                // Navigate back to the LoginActivity
                val intent = Intent(requireActivity(), LoginRegister::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                // Dismiss logout and loading dialogs
                dialog.dismiss()
                loadingDialog.dismiss()

            }, 2000) // 2-second delay for simulation
        }

        // No button click listener
        noButton.setOnClickListener {
            dialog.dismiss() // Just dismiss the dialog
        }

        // Show the dialog
        dialog.show()
    }

    // Set up the loading dialog
    private fun setupLoadingDialog() {
        loadingDialog = Dialog(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null)
        loadingDialog.setContentView(dialogView)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
