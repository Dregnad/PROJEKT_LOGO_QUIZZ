package com.example.projekt_logo_quizz

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var isMusicPlaying = true

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val settingsButton = findViewById<ImageView>(R.id.settingsButton)
        val categoryButton = findViewById<Button>(R.id.categoryButton)
        val musicToggleButton = findViewById<ImageView>(R.id.musicToggleButton)

        findViewById<ImageView>(R.id.btnStatistics).setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.btnAchievements).setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        isMusicPlaying = sharedPreferences.getBoolean("music_state", true)

        // Ustawienie odpowiedniej ikony dźwięku na start
        updateMusicIcon(musicToggleButton)

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        categoryButton.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
        }

        musicToggleButton.setOnClickListener {
            if (isMusicPlaying) {
                stopService(Intent(this, MusicService::class.java))
            } else {
                startService(Intent(this, MusicService::class.java))
            }
            isMusicPlaying = !isMusicPlaying
            saveMusicState(isMusicPlaying)  // Zapisanie nowego stanu muzyki
            updateMusicIcon(musicToggleButton) // Zmiana ikony
        }
    }

    private fun updateMusicIcon(musicToggleButton: ImageView) {
        if (isMusicPlaying) {
            musicToggleButton.setImageResource(R.drawable.volume_sound) // Ikona dźwięku
        } else {
            musicToggleButton.setImageResource(R.drawable.volume_no_sound) // Ikona wyciszenia
        }
    }

    private fun saveMusicState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("music_state", state)
        editor.apply()
    }
}
