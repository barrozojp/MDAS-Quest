package com.codeofduty.mdas_rpg

import android.animation.ObjectAnimator
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import com.codeofduty.mdas_rpg.databinding.ActivityLoginRegisterBinding

class LoginRegister : AppCompatActivity() {

    // Declare the View Binding variable
    private lateinit var binding: ActivityLoginRegisterBinding
    private lateinit var soundPool: SoundPool
    private var tapSoundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the SoundPool
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

    // Detect touch events and play the tap sound
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Play the tap sound when the user touches the screen
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            soundPool.play(tapSoundId, 1f, 1f, 1, 0, 1f)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release() // Release resources when the activity is destroyed
    }

    private fun fadeInContent() {
        binding.contentLayout.alpha = 0f
        binding.contentLayout.visibility = android.view.View.VISIBLE
        val fadeIn = ObjectAnimator.ofFloat(binding.contentLayout, "alpha", 0f, 1f)
        fadeIn.duration = 1500
        fadeIn.start()
    }
}