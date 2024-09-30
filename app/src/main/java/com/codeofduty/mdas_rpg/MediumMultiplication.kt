package com.codeofduty.mdas_rpg

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.media.MediaPlayer
import android.os.Handler
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.codeofduty.mdas_rpg.databinding.ActivityEasyMultiplicationBinding
import com.codeofduty.mdas_rpg.databinding.ActivityMediumMultiplicationBinding
import com.jakewharton.rxbinding2.widget.RxTextView
import kotlin.random.Random


@SuppressLint("CheckResult")
class MediumMultiplication : AppCompatActivity() {
    private lateinit var binding: ActivityMediumMultiplicationBinding
    private var firstValue: Int = 0
    private var secondValue: Int = 0
    private var wrongAttempts: Int = 0
    private var score: Int = 0 // Initialize score variable
    private var timerValue: Int = 0 // Timer variable
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var soundPool: SoundPool
    private var tapSoundId: Int = 0
    private lateinit var backgroundMusic: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediumMultiplicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show the countdown dialog before starting the game
        showCountdownDialog()

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

        // Initialize and start background music
        backgroundMusic = MediaPlayer.create(this, R.raw.ingame_music)
        backgroundMusic.isLooping = true // Loop the music
        backgroundMusic.start() // Start the music
    }

    // Detect touch events and play the tap sound
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Play the tap sound when the user touches the screen
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            soundPool.play(tapSoundId, 1f, 1f, 1, 0, 1f)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun showCountdownDialog() {
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_countdown, null)
        val countdownTextView = dialogView.findViewById<TextView>(R.id.dialog_countdown)

        // Create the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Disable dismiss on outside click
            .create()

        dialog.show()

        val handler = Handler()
        var countdownValue = 5

        // Countdown runnable
        val countdownRunnable = object : Runnable {
            override fun run() {
                countdownValue--
                if (countdownValue >= 0) {
                    countdownTextView.text = countdownValue.toString() // Update countdown text
                    handler.postDelayed(this, 1000) // Update every second
                } else {
                    dialog.dismiss() // Dismiss the dialog
                    startGame() // Start the game after countdown
                }
            }
        }

        handler.postDelayed(countdownRunnable, 1000) // Start countdown
    }

    private fun startGame() {
        generateQuestion()

        // Show the keyboard automatically
        binding.etAnswer.requestFocus() // Request focus on the EditText
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.etAnswer, InputMethodManager.SHOW_IMPLICIT) // Show the keyboard

        // Initialize the timer
        timerValue = 0 // Reset timer value
        handler = Handler()
        runnable = Runnable {
            timerValue++
            updateTimerText() // Update the timer TextView with formatted time
            handler.postDelayed(runnable, 1000) // Call this again after 1 second
        }
        handler.postDelayed(runnable, 1000) // Start the timer after 1 second

        // Observe text changes in the TextInputEditText
        val answerStream = RxTextView.textChanges(binding.etAnswer)
            .skipInitialValue()
            .map { answer -> answer.isNotEmpty() }

        // Enable/disable the button based on text input
        answerStream.subscribe { isValid ->
            binding.btnSubmit.isEnabled = isValid
            binding.btnSubmit.backgroundTintList =
                ContextCompat.getColorStateList(this, if (isValid) R.color.enabled_button_color else android.R.color.darker_gray)
        }

        // Submit button click event
        binding.btnSubmit.setOnClickListener {
            handleAnswer()
        }
    }

    private fun generateQuestion() {
        // Generate random values between 1 and 10
        firstValue = Random.nextInt(1, 51)
        secondValue = Random.nextInt(1, 6)

        // Update the UI
        binding.firstValue.text = firstValue.toString()
        binding.secondValue.text = secondValue.toString()

        // Reset the answer field
        binding.etAnswer.text?.clear()
    }

    private fun handleAnswer() {
        // Get the user's answer from the EditText
        val userAnswer = binding.etAnswer.text.toString().toIntOrNull() // Use toIntOrNull for safety
        val correctAnswer = firstValue * secondValue

        // Check if the user's answer is correct
        if (userAnswer == correctAnswer) {
            score++ // Increment the score
            binding.scoreCounter.text = "Score: $score" // Update the score display
            generateQuestion() // Generate a new question
        } else {
            wrongAttempts++ // Increment the wrong attempts count
            // Update heart counter in the UI
            binding.heartCounter.text = "${3 - wrongAttempts}"

            if (wrongAttempts >= 3) {
                // Show the game over dialog when wrong attempts reach 3
                showGameOverDialog()
            } else {
                generateQuestion() // Generate a new question
            }
        }

        // Clear the EditText after processing the answer
        binding.etAnswer.text?.clear() // Clear the answer field
    }

    private fun showGameOverDialog() {
        // Inflate the custom layout for the game over dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_gameover, null)

        // Find the TextViews to display scores and time (if needed)
        val totalScoreTextView = dialogView.findViewById<TextView>(R.id.dialog_totalScore)
        val totalTimeTextView = dialogView.findViewById<TextView>(R.id.dialog_totalTime)

        // Set the total score text
        totalScoreTextView.text = "Total Score: $score"

        // You can set the total time text if you want
        val formattedTime = String.format("%02d:%02d", timerValue / 60, timerValue % 60)
        totalTimeTextView.text = "Total Time: $formattedTime"

        // Create the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Disable dismiss on outside click
            .create()

        // Set up button listeners
        dialogView.findViewById<ImageView>(R.id.return_home).setOnClickListener {
            // Dismiss the dialog
            dialog.dismiss()
            // Finish this current activity
            finish()
            // Start MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        dialogView.findViewById<ImageView>(R.id.play_again).setOnClickListener {
            // Restart the game
            dialog.dismiss()
            restartGame()
        }

        dialog.show()
    }

    private fun restartGame() {
        // Stop the current timer
        handler.removeCallbacks(runnable)

        // Reset game variables
        wrongAttempts = 0
        score = 0
        timerValue = 0

        // Reset UI elements
        binding.scoreCounter.text = "Score: $score"
        binding.heartCounter.text = "3" // Assuming starting hearts are 3
        binding.etAnswer.text?.clear() // Clear the answer field
        binding.timer.text = "00:00" // Reset timer display

        // Show the countdown dialog again
        showCountdownDialog() // Start the countdown before the game starts
    }

    // Method to update the timer TextView
    private fun updateTimerText() {
        val minutes = timerValue / 60 // Calculate minutes
        val seconds = timerValue % 60 // Calculate seconds

        // Format the time as MM:SS
        val formattedTime = String.format("%02d:%02d", minutes, seconds)
        binding.timer.text = "$formattedTime" // Update the timer TextView
    }

    // Disable the back button
    override fun onBackPressed() {
        // Do nothing when the back button is pressed
    }

    override fun onPause() {
        super.onPause()
        if (::backgroundMusic.isInitialized && backgroundMusic.isPlaying) {
            backgroundMusic.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::backgroundMusic.isInitialized) {
            backgroundMusic.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::backgroundMusic.isInitialized) {
            backgroundMusic.stop()
            backgroundMusic.release()
        }
    }
}