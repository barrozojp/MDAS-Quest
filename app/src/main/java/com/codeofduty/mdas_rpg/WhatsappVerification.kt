package com.codeofduty.mdas_rpg

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.codeofduty.mdas_rpg.databinding.ActivityWhatsappVerificationBinding
import com.google.firebase.database.*
import okhttp3.*
import java.io.IOException

class WhatsappVerification : AppCompatActivity() {

    private lateinit var binding: ActivityWhatsappVerificationBinding
    private lateinit var userPhoneNumber: String
    private var generatedOtp: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWhatsappVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userPhone = sharedPreferences.getString("user_phone", "")

        if (!userPhone.isNullOrEmpty()) {
            userPhoneNumber = userPhone
            sendWhatsappOtp(userPhoneNumber)
        } else {
            Toast.makeText(this, "Phone number not found!", Toast.LENGTH_LONG).show()
        }

        binding.etOtp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val isValid = s?.length == 6
                binding.btnVerify.isEnabled = isValid
                binding.btnVerify.backgroundTintList = ContextCompat.getColorStateList(
                    this@WhatsappVerification,
                    if (isValid) R.color.enabled_button_color else android.R.color.darker_gray
                )
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.cancelTv.setOnClickListener {
            cancelVerification()
        }

        binding.btnVerify.setOnClickListener {
            verifyOtp()
        }

        binding.smsbtn.setOnClickListener {
            val username = intent.getStringExtra("username")
            val loggedInUserId = intent.getStringExtra("loggedInUserId")

            val intent = Intent(this, LoginVerification::class.java).apply {
                putExtra("loggedInUserId", loggedInUserId)
                putExtra("username", username)
                putExtra("userPhoneNumber", userPhoneNumber)
            }
            startActivity(intent)
        }


    }

    private fun cancelVerification() {
        val intent = Intent(this, LoginRegister::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun sendWhatsappOtp(phone: String) {
        val formattedPhone = if (phone.startsWith("+63")) phone else "+63" + phone.removePrefix("0")
        generatedOtp = (100000..999999).random().toString()

        val twilioAccountSid = "AC8ae89366d521d6d8a8f8aa69b4fb5ded"
        val twilioAuthToken = "PLACEHOLDER MUNA"
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("To", "whatsapp:$formattedPhone")
            .add("From", "whatsapp:+14155238886")
            .add("Body", "Your WhatsApp OTP is: $generatedOtp")
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
                        Toast.makeText(applicationContext, "OTP Sent via WhatsApp", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, "OTP Sending Failed: ${response.code} - ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun verifyOtp() {
        val enteredOtp = binding.etOtp.text.toString()

        if (enteredOtp == generatedOtp) {
            val username = intent.getStringExtra("username")
            val loggedInUserId = intent.getStringExtra("loggedInUserId")

            Toast.makeText(this, "OTP Verified. Logging in...", Toast.LENGTH_LONG).show()

            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("loggedInUserId", loggedInUserId)
                putExtra("username", username)
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Invalid OTP! Try again.", Toast.LENGTH_LONG).show()
        }
    }
}
