package com.codeofduty.mdas_rpg

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SwitchCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import java.io.File
import java.io.FileWriter
import java.io.IOException

class SettingsFragment : Fragment() {

    private lateinit var inGameMusicSwitch: SwitchCompat
    private lateinit var vibrationSwitch: SwitchCompat // Add vibration switch
    private lateinit var bgMusicSwitch: SwitchCompat // For background music
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize Shared Preferences
        sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)

        inGameMusicSwitch = view.findViewById(R.id.igmusic_onoff)
        vibrationSwitch = view.findViewById(R.id.vibration_onoff)
        bgMusicSwitch = view.findViewById(R.id.bgmusic_onoff) // Initialize the bg music switch

        // Load saved states
        inGameMusicSwitch.isChecked = sharedPreferences.getBoolean("in_game_music", true)
        vibrationSwitch.isChecked = sharedPreferences.getBoolean("vibration", true) // Load vibration state
        bgMusicSwitch.isChecked = sharedPreferences.getBoolean("bg_music", true) // Load bg music state

        inGameMusicSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("in_game_music", isChecked).apply()
        }

        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("vibration", isChecked).apply() // Save vibration state
        }

        bgMusicSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("bg_music", isChecked).apply() // Save bg music state
            // Notify MainActivity to update the music state
            val activity = activity as MainActivity
            activity.setBackgroundMusicState(isChecked)
        }

        val exportButton: AppCompatButton = view.findViewById(R.id.btn_export)
        exportButton.setOnClickListener {
            onExportButtonClick()
        }

        return view
    }

    private fun checkPermissionAndExport() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            exportUserDataToCSV()
            exportUserScoreToCSV()
            exportUserNotesToCSV()
        }
    }

    private fun onExportButtonClick() {
        checkPermissionAndExport()
    }

    private fun exportUserDataToCSV() {
        val dbHelper = DatabaseHelper(requireContext())
        val db = dbHelper.readableDatabase

        // Create "MDAS Data" folder in external storage
        val dir = File(Environment.getExternalStorageDirectory(), "Download/MDAS-Data")
        if (!dir.exists()) {
            dir.mkdirs() // Create the directory if it doesn't exist
        }

        // Define the CSV file for user data
        val csvFile = File(dir, "UserDataExport.csv")

        try {
            val fileWriter = FileWriter(csvFile)
            // Write headers
            fileWriter.append("Username,Password\n")

            // Export Users
            val userCursor = db.rawQuery("SELECT ${DatabaseHelper.COLUMN_USERNAME}, ${DatabaseHelper.COLUMN_PASSWORD} FROM ${DatabaseHelper.TABLE_USERS}", null)
            if (userCursor.moveToFirst()) {
                do {
                    val username = userCursor.getString(0)
                    val password = userCursor.getString(1)
                    fileWriter.append("$username,$password\n")
                } while (userCursor.moveToNext())
            }
            userCursor.close()

            fileWriter.flush()
            fileWriter.close()

            // Inform the user about success
            Toast.makeText(requireContext(), "User Data CSV file created: ${csvFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            // Handle the error using a Toast
            Toast.makeText(requireContext(), "Error writing User Data CSV file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportUserScoreToCSV() {
        val dbHelper = DatabaseHelper(requireContext())
        val db = dbHelper.readableDatabase

        // Create "MDAS Data" folder in external storage
        val dir = File(Environment.getExternalStorageDirectory(), "Download/MDAS-Data")
        if (!dir.exists()) {
            dir.mkdirs() // Create the directory if it doesn't exist
        }

        // Define the CSV file for user scores
        val csvFile = File(dir, "UserScoreExport.csv")

        try {
            val fileWriter = FileWriter(csvFile)
            // Write headers
            fileWriter.append("Username,Operation Difficulty,Score\n")

            // Export Game Records
            val gameCursor = db.rawQuery("SELECT ${DatabaseHelper.COLUMN_GAME_USERNAME}, ${DatabaseHelper.COLUMN_OPERATION_DIFFICULTY}, ${DatabaseHelper.COLUMN_SCORE} FROM ${DatabaseHelper.TABLE_GAME}", null)
            if (gameCursor.moveToFirst()) {
                do {
                    val gameUsername = gameCursor.getString(0)
                    val operationDifficulty = gameCursor.getString(1)
                    val score = gameCursor.getInt(2)
                    fileWriter.append("$gameUsername,$operationDifficulty,$score\n")
                } while (gameCursor.moveToNext())
            }
            gameCursor.close()

            fileWriter.flush()
            fileWriter.close()

            // Inform the user about success
            Toast.makeText(requireContext(), "User Score CSV file created: ${csvFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            // Handle the error using a Toast
            Toast.makeText(requireContext(), "Error writing User Score CSV file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportUserNotesToCSV() {
        val dbHelper = DatabaseHelper(requireContext())
        val db = dbHelper.readableDatabase

        // Create "MDAS Data" folder in external storage
        val dir = File(Environment.getExternalStorageDirectory(), "Download/MDAS-Data")
        if (!dir.exists()) {
            dir.mkdirs() // Create the directory if it doesn't exist
        }

        // Define the CSV file for user notes
        val csvFile = File(dir, "UserNotesExport.csv")

        try {
            val fileWriter = FileWriter(csvFile)
            // Write headers
            fileWriter.append("Username,Note Title,Content\n")

            // Export Notes
            val noteCursor = db.rawQuery("SELECT ${DatabaseHelper.COLUMN_NOTE_USER}, ${DatabaseHelper.COLUMN_TITLE}, ${DatabaseHelper.COLUMN_CONTENT} FROM ${DatabaseHelper.TABLE_NOTES}", null)
            if (noteCursor.moveToFirst()) {
                do {
                    val noteUser = noteCursor.getString(0)
                    val noteTitle = noteCursor.getString(1)
                    val noteContent = noteCursor.getString(2)
                    fileWriter.append("$noteUser,$noteTitle,$noteContent\n")
                } while (noteCursor.moveToNext())
            }
            noteCursor.close()

            fileWriter.flush()
            fileWriter.close()

            // Inform the user about success
            Toast.makeText(requireContext(), "User Notes CSV file created: ${csvFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            // Handle the error using a Toast
            Toast.makeText(requireContext(), "Error writing User Notes CSV file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportUserDataToCSV()
                exportUserScoreToCSV()
                exportUserNotesToCSV()
            } else {
                Toast.makeText(requireContext(), "Permission denied to write external storage", Toast.LENGTH_SHORT).show() // Inform user of denied permission
            }
        }
    }
}
