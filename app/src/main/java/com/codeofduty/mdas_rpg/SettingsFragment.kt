package com.codeofduty.mdas_rpg

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SwitchCompat

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

        return view
    }
}


