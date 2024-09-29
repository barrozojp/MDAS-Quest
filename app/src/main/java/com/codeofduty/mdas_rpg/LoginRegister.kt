package com.codeofduty.mdas_rpg

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.codeofduty.mdas_rpg.databinding.ActivityLoginRegisterBinding

class LoginRegister : AppCompatActivity() {

    // Declare the View Binding variable
    private lateinit var binding: ActivityLoginRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityLoginRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle login button click
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java)) // Redirect to LoginActivity
        }

        // Handle register button click
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java)) // Redirect to LoginActivity
        }

        // Fade in animation
        fadeInContent()
    }

    private fun fadeInContent() {
        binding.contentLayout.alpha = 0f
        binding.contentLayout.visibility = android.view.View.VISIBLE
        val fadeIn = ObjectAnimator.ofFloat(binding.contentLayout, "alpha", 0f, 1f)
        fadeIn.duration = 1500
        fadeIn.start()
    }
}