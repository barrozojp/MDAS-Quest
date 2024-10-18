package com.codeofduty.mdas_rpg

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.media.MediaPlayer
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
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
    private var correctAnswerCount: Int = 0
    private var heartCounter: Int = 3
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var operationDifficulty: String
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediumMultiplicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        // Get the passed operation difficulty
        operationDifficulty = intent.getStringExtra("operation_difficulty") ?: "Unknown"

        // Load preferences
        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isInGameMusicEnabled = sharedPreferences.getBoolean("in_game_music", true)

        // Show the countdown dialog before starting the game
        showCountdownDialog()

        // Add click listener for the menu game button
        binding.menuGame.setOnClickListener {
            showPauseDialog()
        }

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

        // Check if in-game music should be played
        if (isInGameMusicEnabled) {
            backgroundMusic.start() // Start the music if enabled
        } else {
            backgroundMusic.pause() // Pause the music if disabled
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
            correctAnswerCount++ // Increment the correct answer count

            // Check for bonus life every 10 correct answers
            if (correctAnswerCount % 10 == 0) {
                heartCounter++ // Increment heartCounter directly
                binding.heartCounter.text = "$heartCounter" // Update the hearts counter in the UI

                showBonusLifeDialog() // Show bonus life dialog
            }

            // Handle bonus points for every 5 correct answers
            if (correctAnswerCount % 7 == 0) {
                score += 5 // Add the bonus points

                // Inflate the dialog layout
                val dialogView = layoutInflater.inflate(R.layout.dialog_bonus_points, null)

                // Create the dialog
                val bonusDialog = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false) // Allow dialog to be dismissed when touching outside
                    .create()

                // Show the dialog
                bonusDialog.show()

                // Set the background of the dialog to transparent
                bonusDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                // Dismiss the dialog after 800 ms
                Handler().postDelayed({
                    if (bonusDialog.isShowing) {
                        bonusDialog.dismiss()
                    }
                }, 1300) // Display for 0.8 seconds
            }

            binding.scoreCounter.text = "Score: $score" // Update the score display
            generateQuestion() // Generate a new question
        } else {
            wrongAttempts++ // Increment the wrong attempts count

            // Decrease the heartCounter and update the UI
            heartCounter--
            binding.heartCounter.text = "$heartCounter"

            // Show heart lost dialog when the user loses a life
            showHeartLostDialog()

            if (heartCounter <= 0) {
                // Show the game over dialog when hearts are 0 or below
                showGameOverDialog()
            } else {
                generateQuestion() // Generate a new question
            }
        }

        // Clear the EditText after processing the answer
        binding.etAnswer.text?.clear() // Clear the answer field
    }

    private fun showGameOverDialog() {
        // Retrieve the username from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE) // Use the correct SharedPreferences
        val username = sharedPreferences.getString("username", "") ?: ""


        // Insert the game record into the database using the username
        val isInserted = databaseHelper.insertGameRecord(username, operationDifficulty, score)

        // Inflate the custom layout for the game over dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_gameover, null)
        val totalScoreTextView = dialogView.findViewById<TextView>(R.id.dialog_totalScore)
        val totalTimeTextView = dialogView.findViewById<TextView>(R.id.dialog_totalTime)

        totalScoreTextView.text = "Total Score: $score"
        val formattedTime = String.format("%02d:%02d", timerValue / 60, timerValue % 60)
        totalTimeTextView.text = "Total Time: $formattedTime"

        if (!isInserted) {
            totalScoreTextView.text = "Error saving score"
        }

        // Show the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<ImageView>(R.id.return_home).setOnClickListener {
            dialog.dismiss()
            finish()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        dialogView.findViewById<ImageView>(R.id.play_again).setOnClickListener {
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
        heartCounter = 3 // Reset heartCounter here to ensure logic matches UI

        // Reset UI elements
        binding.scoreCounter.text = "Score: $score"
        binding.heartCounter.text = "$heartCounter" // Ensure this matches the reset heartCounter
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

    private fun showHeartLostDialog() {
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_heart_lost, null)

        // Create the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Set the background of the dialog window to be transparent
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()

        // Vibrate when the dialog appears if enabled
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val isVibrationEnabled = sharedPreferences.getBoolean("vibration", true) // Check vibration preference

        if (isVibrationEnabled) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // Vibration effect for Android O and above
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)) // Vibrate for 500 milliseconds
            } else {
                // Vibration for devices below Android O
                vibrator.vibrate(500) // Vibrate for 500 milliseconds
            }
        }

        // Dismiss the dialog after 500 milliseconds
        Handler().postDelayed({
            dialog.dismiss()
        }, 500)
    }
    private fun showBonusLifeDialog() {
        // Inflate the custom layout for the bonus life dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_bonus_life, null)

        // Create the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Set the background of the dialog window to be transparent
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()

        // Dismiss the dialog after 500 milliseconds
        Handler().postDelayed({
            dialog.dismiss()
        }, 800)
    }

    private fun showPauseDialog() {
        // Inflate the custom layout for the dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_pausegame, null) // Adjust this name if necessary

        // Create the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Prevent dismissing on outside click
            .create()

        // Find buttons in the dialog view
        val resumeButton = dialogView.findViewById<ImageView>(R.id.resume)
        val returnHomeButton = dialogView.findViewById<ImageView>(R.id.return_home)

        // Set up the click listener for the resume button
        resumeButton.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog to resume the game
        }

        // Set up the click listener for the return home button
        returnHomeButton.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog
            showGameOverDialog()
        }

        // Show the dialog
        dialog.show()
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