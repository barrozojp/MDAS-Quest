package com.codeofduty.mdas_rpg

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.codeofduty.mdas_rpg.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var soundPool: SoundPool
    private var tapSoundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.bottomNavigation.background = null
        binding.bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.bottom_home -> openFragment(HomeFragment())
                R.id.bottom_leaderboard -> openFragment(LeaderboardFragment())
                R.id.bottom_notepad -> openFragment(NotepadFragment())
                R.id.bottom_settings -> openFragment(SettingsFragment())
                R.id.bottom_profile -> openFragment(ProfileFragment())
            }
            true
        })

        fragmentManager = supportFragmentManager
        openFragment(HomeFragment()) // Set initial fragment
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

    // Disable the back button
    override fun onBackPressed() {
        // Do nothing when the back button is pressed
    }


    private fun openFragment(fragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}
