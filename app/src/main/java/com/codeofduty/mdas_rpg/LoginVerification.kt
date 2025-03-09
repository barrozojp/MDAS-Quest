package com.codeofduty.mdas_rpg

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.codeofduty.mdas_rpg.databinding.ActivityLoginVerificationBinding
import okhttp3.*
import java.io.IOException
import com.google.firebase.database.*

class LoginVerification : AppCompatActivity() {

    private lateinit var binding: ActivityLoginVerificationBinding
    private lateinit var userPhoneNumber: String
    private var generatedOtp: String = ""
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("loggedInUserId", null)

        if (!userId.isNullOrEmpty()) {
            getUserPhoneNumber(userId)
        } else {
            Toast.makeText(this, "User ID not found!", Toast.LENGTH_LONG).show()
        }

        // Enable btn_verify only when OTP is entered
        binding.etOtp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val isValid = s?.length == 6
                binding.btnVerify.isEnabled = isValid
                binding.btnVerify.backgroundTintList = ContextCompat.getColorStateList(
                    this@LoginVerification,
                    if (isValid) R.color.enabled_button_color else android.R.color.darker_gray
                )
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Cancel button click listener
        binding.cancelTv.setOnClickListener {
            cancelLogin()
        }

        // Verify button click listener
        binding.btnVerify.setOnClickListener {
            verifyOtp()
        }


    }
    private fun cancelLogin() {
        // Clear SharedPreferences to log out the user
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Navigate back to the LoginRegister activity
        val intent = Intent(this, LoginRegister::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun sendOtp(phone: String) {
        val formattedPhone = if (phone.startsWith("+63")) phone else "+63" + phone.removePrefix("0")
        generatedOtp = (100000..999999).random().toString()

        val twilioAccountSid = "AC8ae89366d521d6d8a8f8aa69b4fb5ded"
        val twilioAuthToken = "d7e6856a6bd1fc62dd3a2a2b4a3540b2"
        val messagingServiceSid = "MGbc9add2e524ba0b26c81b3eaeed5260c"

        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("To", formattedPhone)
            .add("MessagingServiceSid", messagingServiceSid)
            .add("Body", "Your OTP for login verification is: $generatedOtp")
            .build()

        val request = Request.Builder()
            .url("https://api.twilio.com/2010-04-01/Accounts/$twilioAccountSid/Messages.json")
            .post(requestBody)
            .header("Authorization", Credentials.basic(twilioAccountSid, twilioAuthToken))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to send OTP: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(applicationContext, "OTP Sent to $formattedPhone", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, "OTP Sending Failed: ${response.code} - ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }


        })
    }


    private fun getUserPhoneNumber(userId: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)

        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                userPhoneNumber = snapshot.child("phone").value.toString()
                Toast.makeText(this, "User Phone: $userPhoneNumber", Toast.LENGTH_LONG).show()

                // Send OTP to retrieved phone number
                sendOtp(userPhoneNumber)
            } else {
                Toast.makeText(this, "Phone number not found!", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to get phone number!", Toast.LENGTH_LONG).show()
        }
    }

    private fun verifyOtp() {
        val enteredOtp = binding.etOtp.text.toString()

        if (enteredOtp == generatedOtp) {
            Toast.makeText(this, "OTP Verified. Logging in...", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Invalid OTP! Try again.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logoutUser()
    }

    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        sharedPreferences.edit().remove("loggedInUserId").apply()
        sharedPreferences.edit().remove("username").apply()
    }

}
