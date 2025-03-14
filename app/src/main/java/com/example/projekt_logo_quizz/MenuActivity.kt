package com.example.projekt_logo_quizz

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MenuActivity : AppCompatActivity() {
    private var isMusicPlaying = true

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val categoryButton = findViewById<Button>(R.id.categoryButton)
        val musicToggleButton = findViewById<Button>(R.id.musicToggleButton)

        // Uruchomienie muzyki przy starcie aplikacji
        startService(Intent(this, MusicService::class.java))

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
                musicToggleButton.text = "Włącz muzykę"
            } else {
                startService(Intent(this, MusicService::class.java))
                musicToggleButton.text = "Wyłącz muzykę"
            }
            isMusicPlaying = !isMusicPlaying
        }
    }
}
