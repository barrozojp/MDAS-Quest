package com.codeofduty.mdas_rpg

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.codeofduty.mdas_rpg.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import com.codeofduty.mdas_rpg.databinding.ActivityRegisterBinding
import com.jakewharton.rxbinding2.widget.RxTextView

@SuppressLint("CheckResult")
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var soundPool: SoundPool
    private var tapSoundId: Int = 0
    private lateinit var database: DatabaseReference
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var loadingDialog: Dialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference

        // Initialize the database helper
        dbHelper = DatabaseHelper(this)

        // Set up loading dialog
        setupLoadingDialog()

        // Set up the SoundPool for tap sound
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load the tap sound from the raw folder
        tapSoundId = soundPool.load(this, R.raw.tap_sound, 1)

        // Username Validation (minimum 6 characters)
        val usernameStream = RxTextView.textChanges(binding.etUsername)
            .skipInitialValue()
            .map { username -> username.isEmpty() || username.length < 6 } // Add length check
        usernameStream.subscribe { showTextMinimalAlert(it, "Username") }

        // Password Validation (minimum 6 characters)
        val passwordStream = RxTextView.textChanges(binding.etPassword)
            .skipInitialValue()
            .map { password -> password.isEmpty() || password.length < 6 } // Add length check
        passwordStream.subscribe { showTextMinimalAlert(it, "Password") }

        // Confirm Password Validation (check if it matches password)
        val confirmPasswordStream = RxTextView.textChanges(binding.etConfpassword)
            .skipInitialValue()
            .map { confirmPassword -> confirmPassword.isEmpty() || confirmPassword.toString() != binding.etPassword.text.toString() }
        confirmPasswordStream.subscribe { showTextMinimalAlert(it, "Confirm Password") }

        // Button Enable True or False
        val invalidFieldStream = io.reactivex.Observable.combineLatest(
            usernameStream,
            passwordStream,
            confirmPasswordStream,
            { usernameInvalid: Boolean, passwordInvalid: Boolean, confirmPasswordInvalid: Boolean ->
                !usernameInvalid && !passwordInvalid && !confirmPasswordInvalid
            })
        invalidFieldStream.subscribe { isValid ->
            binding.btnLogin.isEnabled = isValid
            binding.btnLogin.backgroundTintList =
                ContextCompat.getColorStateList(this, if (isValid) R.color.enabled_button_color else android.R.color.darker_gray)
        }

        // Register button click event
        binding.btnLogin.setOnClickListener {
            loadingDialog.show()

            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            database.child("users").get()
                .addOnSuccessListener { dataSnapshot ->
                    var usernameExists = false

                    for (userSnapshot in dataSnapshot.children) {
                        val existingUsername = userSnapshot.child("username").value.toString()
                        if (existingUsername == username) {
                            usernameExists = true
                            break
                        }
                    }

                    if (usernameExists) {
                        Toast.makeText(this, "User already exists!", Toast.LENGTH_SHORT).show()
                        loadingDialog.dismiss()
                    } else {
                        if (dbHelper.checkUserExists(username)) {
                            Toast.makeText(this, "User already exists!", Toast.LENGTH_SHORT).show()
                            loadingDialog.dismiss()
                        } else {
                            val userId = database.child("users").push().key

                            if (userId != null) {
                                val userMap = hashMapOf(
                                    "userId" to userId,
                                    "username" to username,
                                    "password" to password
                                )

                                database.child("users").child(userId).setValue(userMap)
                                    .addOnSuccessListener {
                                        dbHelper.insertUser(username, password)
                                        Toast.makeText(this, "Registration Complete", Toast.LENGTH_SHORT).show()

                                        loadingDialog.dismiss()
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Failed to save user to Firebase!", Toast.LENGTH_SHORT).show()
                                        loadingDialog.dismiss()
                                    }
                            } else {
                                Toast.makeText(this, "Error generating user ID", Toast.LENGTH_SHORT).show()
                                loadingDialog.dismiss()
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error checking username!", Toast.LENGTH_SHORT).show()
                    loadingDialog.dismiss()
                }
        }

        binding.backTv.setOnClickListener {
            finish() // or implement any navigation logic if needed
        }
    }

    // Detect touch events and play the tap sound
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Play the tap sound when the user touches the screen
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            soundPool.play(tapSoundId, 1f, 1f, 1, 0, 1f)
        }
        return super.dispatchTouchEvent(ev)
    }

    // Function to set up the loading dialog
    private fun setupLoadingDialog() {
        loadingDialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null)
        loadingDialog.setContentView(dialogView)
        loadingDialog.setCancelable(false) // Make it non-cancelable while loading
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Transparent background
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        when (text) {
            "Username" -> binding.etUsername.error = if (isNotValid) "$text must be at least 6 characters!" else null
            "Password" -> binding.etPassword.error = if (isNotValid) "$text must be at least 6 characters!" else null
            "Confirm Password" -> binding.etConfpassword.error = if (isNotValid) {
                if (binding.etConfpassword.text?.isEmpty() == true) "$text cannot be empty!"
                else "$text must match Password!"
            } else null
        }
    }
}
