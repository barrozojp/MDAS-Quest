package com.codeofduty.mdas_rpg

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.codeofduty.mdas_rpg.databinding.ActivityLoginBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.jakewharton.rxbinding2.widget.RxTextView

@SuppressLint("CheckResult")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var soundPool: SoundPool
    private var tapSoundId: Int = 0
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var loadingDialog: Dialog
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        // Initialize SQLite Database Helper
        dbHelper = DatabaseHelper(this)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

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
            .map { username -> username.isEmpty() || username.length < 6 }
        usernameStream.subscribe { showTextMinimalAlert(it, "Username") }

        // Password Validation (minimum 6 characters)
        val passwordStream = RxTextView.textChanges(binding.etPassword)
            .skipInitialValue()
            .map { password -> password.isEmpty() || password.length < 6 }
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
            loadingDialog.show()

            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            database.child("users").get()
                .addOnSuccessListener { dataSnapshot ->
                    var userFound = false
                    var userId: String? = null

                    for (userSnapshot in dataSnapshot.children) {
                        val existingUsername = userSnapshot.child("username").value.toString()
                        val existingPassword = userSnapshot.child("password").value.toString()

                        if (existingUsername == username && existingPassword == password) {
                            userFound = true
                            userId = userSnapshot.child("userId").value.toString()
                            break
                        }
                    }

                    if (userFound) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                        sharedPreferences.edit().putString("loggedInUserId", userId).apply()
                        sharedPreferences.edit().putString("username", username).apply()

                        // Show the userId in a toast
                        Toast.makeText(this, "User ID: $userId", Toast.LENGTH_SHORT).show()

                        // Sync to SQLite if missing
                        if (!dbHelper.checkUserExists(username)) {
                            dbHelper.insertUser(username, password)
                        }

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }

                    loadingDialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error checking credentials!", Toast.LENGTH_SHORT).show()
                    loadingDialog.dismiss()
                }
        }

        binding.backTv.setOnClickListener {
            finish()
        }
    }

    private fun setupLoadingDialog() {
        loadingDialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null)
        loadingDialog.setContentView(dialogView)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
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
