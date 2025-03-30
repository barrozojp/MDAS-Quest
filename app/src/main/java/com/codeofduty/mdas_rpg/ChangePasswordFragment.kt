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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.os.CountDownTimer
import com.github.kittinunf.fuel.Fuel
import org.json.JSONArray
import org.json.JSONObject

class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var loadingDialog: Dialog
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

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


        // Email Code Validation (6 digits, not empty)
        val emailCodeStream = RxTextView.textChanges(binding.etEmailCode)
            .skipInitialValue()
            .map { emailCode -> emailCode.length != 6 }

        emailCodeStream.subscribe { showTextMinimalAlert(it, "Email Verification Code") }

        // Button Enable True or False
        val invalidFieldStream = Observable.combineLatest(
            currentPasswordStream,
            newPasswordStream,
            confirmNewPasswordStream,
            emailCodeStream, // Include email code validation
            { currentInvalid: Boolean, newInvalid: Boolean, confirmInvalid: Boolean, emailCodeInvalid: Boolean ->
                !currentInvalid && !newInvalid && !confirmInvalid && !emailCodeInvalid
            }
        )

        invalidFieldStream.subscribe { isValid ->
            binding.btnSavechanges.isEnabled = isValid
            binding.btnSavechanges.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), if (isValid) R.color.enabled_button_color else android.R.color.darker_gray)
        }

        binding.btnGetcode.setOnClickListener {
            loadingDialog.show()

            val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val userId = sharedPreferences.getString("loggedInUserId", "") ?: ""

            if (userId.isNotEmpty()) {
                val userRef = database.child("users").child(userId).child("email")
                userRef.get().addOnSuccessListener { snapshot ->
                    val userEmail = snapshot.value.toString()

                    // Generate OTP
                    val generatedOtp = generateOtp()

                    // Store OTP in SharedPreferences for verification
                    sharedPreferences.edit().putString("storedOtp", generatedOtp).apply()

                    // Send OTP via email
                    sendVerificationEmail(userEmail, generatedOtp)

                    Toast.makeText(requireContext(), "OTP sent to $userEmail", Toast.LENGTH_SHORT).show()

                    // Disable button and start timer
                    binding.btnGetcode.isEnabled = false
                    binding.btnGetcode.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.disabled_button_color))
                    binding.resendTimer.visibility = View.VISIBLE

                    object : CountDownTimer(30000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            binding.resendTimer.text = (millisUntilFinished / 1000).toString()
                        }

                        override fun onFinish() {
                            binding.btnGetcode.isEnabled = true
                            binding.btnGetcode.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.enabled_button_color))
                            binding.resendTimer.visibility = View.GONE
                        }
                    }.start()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to retrieve email", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                loadingDialog.dismiss()
            }, 3000)
        }


        // Save Changes button click event
        binding.btnSavechanges.setOnClickListener {
            loadingDialog.show()

            val currentPassword = binding.etCurrentpassword.text.toString()
            val newPassword = binding.etNewpassword.text.toString()
            val enteredOtp = binding.etEmailCode.text.toString() // Get user input OTP

            val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val storedOtp = sharedPreferences.getString("storedOtp", "") ?: ""

            if (enteredOtp == storedOtp) { // Verify OTP before proceeding
                val currentUserId = sharedPreferences.getString("loggedInUserId", "") ?: ""

                val userRef = database.child("users").child(currentUserId)
                userRef.get().addOnSuccessListener { snapshot ->
                    val storedPassword = snapshot.child("password").value.toString()

                    if (storedPassword == currentPassword) {
                        userRef.child("password").setValue(newPassword).addOnCompleteListener { task ->
                            Handler(Looper.getMainLooper()).postDelayed({
                                loadingDialog.dismiss()

                                if (task.isSuccessful) {
                                    Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), "Failed to update password.", Toast.LENGTH_SHORT).show()
                                }
                            }, 2000)
                        }
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadingDialog.dismiss()
                            Toast.makeText(requireContext(), "Incorrect current password.", Toast.LENGTH_SHORT).show()
                        }, 2000)
                    }
                }
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
                }, 2000)
            }
        }

        return binding.root
    }

    private fun generateOtp(): String {
        return (100000..999999).random().toString()
    }

    private fun sendVerificationEmail(userEmail: String, otp: String) {
        val apiKey = "PLACEHOLDER MUNA" // Replace with actual API key
        val senderEmail = "mdasquestverifiy@gmail.com"

        val personalizationsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("to", JSONArray().apply {
                    put(JSONObject().put("email", userEmail))
                })
                put("subject", "MDAS Account Verification")
            })
        }

        val contentArray = JSONArray().apply {
            put(JSONObject().apply {
                put("type", "text/plain")
                put("value", "Is this you trying to change your account information?\n\nVerification Code: $otp")
            })
        }

        val emailData = JSONObject().apply {
            put("personalizations", personalizationsArray)
            put("from", JSONObject().put("email", senderEmail))
            put("content", contentArray)
        }

        Fuel.post("https://api.sendgrid.com/v3/mail/send")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .body(emailData.toString())
            .response { _, response, result ->
                activity?.runOnUiThread {
                    result.fold(
                        { Toast.makeText(requireContext(), "Email Sent!", Toast.LENGTH_SHORT).show() },
                        { error ->
                            Toast.makeText(requireContext(), "Failed to send email: ${response.statusCode} ${String(error.response.data)}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
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
            "Email Verification Code" -> binding.etEmailCode.error = if (isNotValid) "$text must be exactly 6 digits!" else null

        }
    }
}

