package com.codeofduty.mdas_rpg

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.codeofduty.mdas_rpg.databinding.ActivityEasyMultiplicationBinding
import com.jakewharton.rxbinding2.widget.RxTextView
import kotlin.random.Random

@SuppressLint("CheckResult")
class EasyMultiplication : AppCompatActivity() {

    private lateinit var binding: ActivityEasyMultiplicationBinding
    private var firstValue: Int = 0
    private var secondValue: Int = 0
    private var wrongAttempts: Int = 0
    private var score: Int = 0 // Initialize score variable
    private var timerValue: Int = 0 // Timer variable
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEasyMultiplicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show the countdown dialog before starting the game
        showCountdownDialog()
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
            binding.btnLogin.isEnabled = isValid
            binding.btnLogin.backgroundTintList =
                ContextCompat.getColorStateList(this, if (isValid) R.color.enabled_button_color else android.R.color.darker_gray)
        }

        // Submit button click event
        binding.btnLogin.setOnClickListener {
            handleAnswer()
        }
    }

    private fun generateQuestion() {
        // Generate random values between 1 and 10
        firstValue = Random.nextInt(1, 11)
        secondValue = Random.nextInt(1, 11)

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
                // Handle game over (e.g., show a message or go to another activity)
                // You can show a Toast or a dialog here
            } else {
                generateQuestion() // Generate a new question
            }
        }

        // Clear the EditText after processing the answer
        binding.etAnswer.text?.clear() // Clear the answer field
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

}
