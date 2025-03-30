package com.codeofduty.mdas_rpg

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
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
import com.github.kittinunf.fuel.Fuel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("CheckResult")
class ChangeUsernameFragment : Fragment() {

    private lateinit var binding: FragmentChangeUsernameBinding
    private lateinit var loadingDialog: Dialog
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChangeUsernameBinding.inflate(inflater, container, false)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

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

        // Email Code Validation (6 digits, not empty)
        val emailCodeStream = RxTextView.textChanges(binding.etEmailCode)
            .skipInitialValue()
            .map { emailCode -> emailCode.length != 6 }

        emailCodeStream.subscribe { showTextMinimalAlert(it, "Email Verification Code") }

        // Button Enable True or False
        val invalidFieldStream = Observable.combineLatest(
            usernameStream,
            passwordStream,
            emailCodeStream,
            { usernameInvalid: Boolean, passwordInvalid: Boolean, emailCodeInvalid: Boolean ->
                !usernameInvalid && !passwordInvalid && !emailCodeInvalid
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

            val newUsername = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            val enteredOtp = binding.etEmailCode.text.toString() // Get user input OTP

            val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val storedOtp = sharedPreferences.getString("storedOtp", "") ?: ""

            if (enteredOtp == storedOtp) { // Verify OTP before proceeding
                val userId = sharedPreferences.getString("loggedInUserId", "") ?: ""

                val userRef = database.child("users").child(userId)
                userRef.get().addOnSuccessListener { snapshot ->
                    val existingPassword = snapshot.child("password").value.toString()

                    if (existingPassword == password) {
                        userRef.child("username").setValue(newUsername).addOnCompleteListener { task ->
                            Handler(Looper.getMainLooper()).postDelayed({
                                loadingDialog.dismiss()

                                if (task.isSuccessful) {
                                    sharedPreferences.edit().putString("username", newUsername).apply()
                                    binding.tvUsername.text = newUsername
                                    Toast.makeText(requireContext(), "Username updated successfully!", Toast.LENGTH_SHORT).show()

                                    updateNotesUsername(currentUsername, newUsername)
                                    updateGameUsername(currentUsername, newUsername)
                                    restartFragment()
                                } else {
                                    Toast.makeText(requireContext(), "Failed to update username.", Toast.LENGTH_SHORT).show()
                                }
                            }, 2000)
                        }
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadingDialog.dismiss()
                            Toast.makeText(requireContext(), "Incorrect password.", Toast.LENGTH_SHORT).show()
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

    private fun updateGameUsername(currentUsername: String, newUsername: String) {
        // Get reference to the game_table
        val gameTableRef = database.child("game_table")

        // Query to find all games where game_username matches currentUsername
        gameTableRef.orderByChild("game_username").equalTo(currentUsername).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (gameSnapshot in snapshot.children) {
                    // Update the game_username for each matching entry
                    gameSnapshot.ref.child("game_username").setValue(newUsername).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Game username updated successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to update game username.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to update game username.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to update note_user in the allnotes table
    private fun updateNotesUsername(currentUsername: String, newUsername: String) {
        val notesRef = database.child("allnotes")
        notesRef.orderByChild("note_user").equalTo(currentUsername).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Debug: Found notes for currentUsername
                Toast.makeText(requireContext(), "Found notes for $currentUsername", Toast.LENGTH_SHORT).show()
                for (noteSnapshot in snapshot.children) {
                    noteSnapshot.ref.child("note_user").setValue(newUsername).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Debug: Successfully updated note_user for note
                            Toast.makeText(requireContext(), "Note updated with new username", Toast.LENGTH_SHORT).show()
                        } else {
                            // Debug: Failed to update note_user for a note
                            Toast.makeText(requireContext(), "Failed to update note_user for note", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                // Debug: No notes found for currentUsername
                Toast.makeText(requireContext(), "No notes found for $currentUsername", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            // Debug: Firebase read failure
            Toast.makeText(requireContext(), "Failed to fetch notes: ${exception.message}", Toast.LENGTH_SHORT).show()
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
            "Email Verification Code" -> binding.etEmailCode.error = if (isNotValid) "$text must be exactly 6 digits!" else null
        }
    }
}
