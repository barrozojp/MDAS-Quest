package com.codeofduty.mdas_rpg

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashScreen : AppCompatActivity() {

    private val SPLASH_TIME: Long = 6000
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        Handler().postDelayed({
            // Check if the user is logged in
            val userId = sharedPreferences.getInt("loggedInUserId", -1)
            if (userId != -1) {
                // User is logged in, navigate to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // No user is logged in, navigate to LoginRegister
                startActivity(Intent(this, LoginRegister::class.java))
            }
            finish() // Call finish() to prevent going back to the splash screen
        }, SPLASH_TIME)
    }
}