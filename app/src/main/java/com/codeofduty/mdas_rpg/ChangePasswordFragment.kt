package com.codeofduty.mdas_rpg

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
import com.codeofduty.mdas_rpg.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var loadingDialog: Dialog
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper(requireContext())

        // Set up loading dialog
        setupLoadingDialog()

        // Password Validation
        val currentPasswordStream = RxTextView.textChanges(binding.etCurrentpassword)
            .skipInitialValue()
            .map { password -> password.isEmpty() || password.length < 6 }
        currentPasswordStream.subscribe { showTextMinimalAlert(it, "Current Password") }

         // New Password Validation
        val newPasswordStream = RxTextView.textChanges(binding.etNewpassword)
            .skipInitialValue()
            .map { password -> password.isEmpty() || password.length < 6 }
        newPasswordStream.subscribe { showTextMinimalAlert(it, "New Password") }

        // Confirm New Password Validation
        val confirmNewPasswordStream = RxTextView.textChanges(binding.etConfnewpassword)
            .skipInitialValue()
            .map { password -> password.toString() != binding.etNewpassword.text.toString() }
        confirmNewPasswordStream.subscribe { showTextMinimalAlert(it, "Confirm New Password") }



        // Button Enable True or False
        val invalidFieldStream = Observable.combineLatest(
            currentPasswordStream,
            newPasswordStream,
            confirmNewPasswordStream,
            { currentInvalid: Boolean, newInvalid: Boolean, confirmInvalid: Boolean ->
                !currentInvalid && !newInvalid && !confirmInvalid
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

            // Retrieve the current password and new password from input fields
            val currentPassword = binding.etCurrentpassword.text.toString()
            val newPassword = binding.etNewpassword.text.toString()

            // Retrieve current username from SharedPreferences
            val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val currentUsername = sharedPreferences.getString("username", "") ?: ""

            // Check if the current password is correct for the logged-in user
            if (dbHelper.checkUserCredentials(currentUsername, currentPassword)) {
                // Update the password in the database
                val updateSuccess = dbHelper.updatePassword(currentUsername, newPassword)

                // Dismiss the loading dialog after a delay
                Handler(Looper.getMainLooper()).postDelayed({
                    loadingDialog.dismiss()

                    if (updateSuccess) {
                        Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update password.", Toast.LENGTH_SHORT).show()
                    }
                }, 2000) // 2-second delay
            } else {
                // Dismiss the loading dialog after a delay
                Handler(Looper.getMainLooper()).postDelayed({
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), "Incorrect current password.", Toast.LENGTH_SHORT).show()
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

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        when (text) {
            "Current Password" -> {
                binding.etCurrentpassword.error = if (isNotValid) "$text must be at least 6 characters!" else null
            }
            "New Password" -> {
                binding.etNewpassword.error = if (isNotValid) "$text must be at least 6 characters!" else null
            }
            "Confirm New Password" -> {
                binding.etConfnewpassword.error = if (isNotValid) "Passwords do not match!" else null
            }
        }
    }


}
