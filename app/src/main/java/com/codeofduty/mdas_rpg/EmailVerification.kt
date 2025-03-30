package com.codeofduty.mdas_rpg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.kittinunf.fuel.Fuel
import org.json.JSONArray

import org.json.JSONObject
import kotlin.random.Random

class EmailVerification : Fragment() {

    private lateinit var etOtp: EditText
    private lateinit var btnVerify: Button
    private var generatedOtp: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_email_verification, container, false)

        etOtp = view.findViewById(R.id.et_otp)
        btnVerify = view.findViewById(R.id.btn_verify)

        // Generate OTP and send email
        generatedOtp = generateOtp()
        sendVerificationEmail("barrozo.johnpaul8@gmail.com", generatedOtp) // Replace with actual user email

        btnVerify.setOnClickListener {
            val enteredOtp = etOtp.text.toString()
            if (enteredOtp == generatedOtp) {
                Toast.makeText(requireContext(), "Verification Successful!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Invalid OTP!", Toast.LENGTH_LONG).show()
            }
        }

        return view
    }

    private fun generateOtp(): String {
        return (100000..999999).random().toString()
    }

    private fun sendVerificationEmail(userEmail: String, otp: String) {
        val apiKey = "placeholder so u wont overreact" // Replace with API key
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
    }}
