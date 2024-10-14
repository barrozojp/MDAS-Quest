package com.codeofduty.mdas_rpg

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
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
import com.codeofduty.mdas_rpg.databinding.ActivityLoginBinding
import com.jakewharton.rxbinding2.widget.RxTextView

@SuppressLint("CheckResult")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var soundPool: SoundPool
    private var tapSoundId: Int = 0
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var loadingDialog: Dialog
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

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

        // Button Enable True or False
        val invalidFieldStream = io.reactivex.Observable.combineLatest(
            usernameStream,
            passwordStream,
            { usernameInvalid: Boolean, passwordInvalid: Boolean ->
                !usernameInvalid && !passwordInvalid
            })
        invalidFieldStream.subscribe { isValid ->
            binding.btnLogin.isEnabled = isValid
            binding.btnLogin.backgroundTintList =
                ContextCompat.getColorStateList(this, if (isValid) R.color.enabled_button_color else android.R.color.darker_gray)
        }

        // Login button click event

        binding.btnLogin.setOnClickListener {
            // Show loading dialog
            loadingDialog.show()

            // Delay for 2 seconds before checking login
            Handler(Looper.getMainLooper()).postDelayed({
                val username = binding.etUsername.text.toString()
                val password = binding.etPassword.text.toString()

                // Check credentials in SQLite
                if (dbHelper.checkUserCredentials(username, password)) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                    // Get the user ID
                    val userId = dbHelper.getUserId(username) // Assuming this method returns the correct userId

                    // Save user ID to SharedPreferences
                    sharedPreferences.edit().putInt("loggedInUserId", userId).apply()

                    // Save the username to SharedPreferences
                    sharedPreferences.edit().putString("username", username).apply()

                    // Navigate to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Call finish() to prevent going back to the login screen
                } else {
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }


                // Dismiss loading dialog
                loadingDialog.dismiss()
            }, 2000) // 2-second delay
        }

        binding.backTv.setOnClickListener {
            finish() // or implement any navigation logic if needed
        }
    }

    // Set up the loading dialog
    private fun setupLoadingDialog() {
        loadingDialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null)
        loadingDialog.setContentView(dialogView)
        loadingDialog.setCancelable(false) // Make it non-cancelable while loading
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Transparent background
    }


    // Detect touch events and play the tap sound
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Play the tap sound when the user touches the screen
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            soundPool.play(tapSoundId, 1f, 1f, 1, 0, 1f)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        when (text) {
            "Username" -> binding.etUsername.error = if (isNotValid) "$text must be at least 6 characters!" else null
            "Password" -> binding.etPassword.error = if (isNotValid) "$text must be at least 6 characters!" else null
        }
    }
}