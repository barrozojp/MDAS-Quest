package com.codeofduty.mdas_rpg

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashScreen : AppCompatActivity() {

    private val SPLASH_TIME: Long = 3000 // Reduced for faster testing
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        Handler().postDelayed({
            val userId = sharedPreferences.getString("loggedInUserId", null) // Get as String

            if (userId != null) {
                // User is logged in, go to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // No user is logged in, go to LoginRegister
                startActivity(Intent(this, LoginRegister::class.java))
            }
            finish() // Prevent returning to SplashScreen
        }, SPLASH_TIME)
    }
}
