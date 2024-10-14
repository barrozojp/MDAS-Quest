package com.codeofduty.mdas_rpg

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import androidx.fragment.app.Fragment
import com.codeofduty.mdas_rpg.databinding.FragmentChangeUsernameBinding

@SuppressLint("CheckResult")
class ChangeUsernameFragment : Fragment() {

    private lateinit var binding: FragmentChangeUsernameBinding
    private lateinit var loadingDialog: Dialog
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChangeUsernameBinding.inflate(inflater, container, false)

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper(requireContext())

        // Set up loading dialog
        setupLoadingDialog()

        // Retrieve and display the current username
        val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val currentUsername = sharedPreferences.getString("username", "") ?: ""
        binding.tvUsername.text = currentUsername // Set the current username

        // Username Validation (minimum 6 characters)
        val usernameStream = RxTextView.textChanges(binding.etUsername)
            .skipInitialValue()
            .map { username -> username.isEmpty() || username.length < 6 }
        usernameStream.subscribe { showTextMinimalAlert(it, "Username") }

        // Password Validation (minimum 6 characters)
        val passwordStream = RxTextView.textChanges(binding.etPassword)
            .skipInitialValue()
            .map { password -> password.isEmpty() || password.length < 6 }
        passwordStream.subscribe { showTextMinimalAlert(it, "Password") }

        // Button Enable True or False
        val invalidFieldStream = Observable.combineLatest(
            usernameStream,
            passwordStream,
            { usernameInvalid: Boolean, passwordInvalid: Boolean ->
                !usernameInvalid && !passwordInvalid
            }
        )
        invalidFieldStream.subscribe { isValid ->
            binding.btnSavechanges.isEnabled = isValid
            binding.btnSavechanges.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), if (isValid) R.color.enabled_button_color else android.R.color.darker_gray)
        }

        // Save Changes button click event
        binding.btnSavechanges.setOnClickListener {
            // Show loading dialog
            loadingDialog.show()

            // Retrieve the new username and password from input fields
            val newUsername = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            // Check if password is correct for the logged-in user
            if (dbHelper.checkUserCredentials(currentUsername, password)) {
                // Update the username in the database
                val updateSuccess = dbHelper.updateUsername(currentUsername, newUsername)

                // Dismiss the loading dialog after a delay
                Handler(Looper.getMainLooper()).postDelayed({
                    loadingDialog.dismiss()

                    if (updateSuccess) {
                        // Update SharedPreferences with the new username
                        sharedPreferences.edit().putString("username", newUsername).apply()
                        binding.tvUsername.text = newUsername // Update displayed username
                        Toast.makeText(requireContext(), "Username updated successfully!", Toast.LENGTH_SHORT).show()

                        // Restart the fragment
                        restartFragment()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update username.", Toast.LENGTH_SHORT).show()
                    }
                }, 2000) // 2-second delay
            } else {
                // Dismiss the loading dialog after a delay
                Handler(Looper.getMainLooper()).postDelayed({
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), "Incorrect password.", Toast.LENGTH_SHORT).show()
                }, 2000) // 2-second delay
            }
        }

        return binding.root
    }

    // Function to set up the loading dialog
    private fun setupLoadingDialog() {
        loadingDialog = Dialog(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null)
        loadingDialog.setContentView(dialogView)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    // Function to restart the fragment
    private fun restartFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ChangeUsernameFragment()) // Ensure 'fragment_container' is the correct ID for your fragment container
            .commit()
    }
    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        when (text) {
            "Username" -> binding.etUsername.error = if (isNotValid) "$text must be at least 6 characters!" else null
            "Password" -> binding.etPassword.error = if (isNotValid) "$text must be at least 6 characters!" else null
        }
    }
}
