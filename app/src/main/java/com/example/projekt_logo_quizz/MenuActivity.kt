package com.example.projekt_logo_quizz

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var isMusicPlaying = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val settingsButton = findViewById<ImageView>(R.id.settingsButton)
        val categoryButton = findViewById<Button>(R.id.categoryButton)
        val musicToggleButton = findViewById<ImageView>(R.id.musicToggleButton)
        val btnStatistics = findViewById<ImageView>(R.id.btnStatistics)
        val exitButton = findViewById<Button>(R.id.button_exit)
        val btnAchievements = findViewById<ImageView>(R.id.btnAchievements)
        val kategorieButton = findViewById<Button>(R.id.button_categories)



        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        isMusicPlaying = sharedPreferences.getBoolean("music_state", true)
        updateMusicIcon(musicToggleButton)

        settingsButton.setOnClickListener {
            animateClick(it)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        categoryButton.setOnClickListener {
            animateClick(it)
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        kategorieButton.setOnClickListener {
            animateClick(it)
            startActivity(Intent(this, KategorieActivity::class.java))
        }

        btnStatistics.setOnClickListener {
            animateClick(it)
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        btnAchievements.setOnClickListener {
            animateClick(it)
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

        exitButton.setOnClickListener {
            animateClick(it)
            finishAffinity()
            System.exit(0)
        }

        musicToggleButton.setOnClickListener {
            animateClick(it)
            toggleMusic(musicToggleButton)
        }
    }

    private fun animateClick(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.85f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f, 1.1f, 1f)
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f, 1f) // Efekt migotania (fade)

        scaleX.duration = 200
        scaleY.duration = 200
        alpha.duration = 200

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY, alpha)
        animatorSet.start()
    }

    private fun toggleMusic(musicToggleButton: ImageView) {
        if (isMusicPlaying) {
            stopService(Intent(this, MusicService::class.java))
        } else {
            startService(Intent(this, MusicService::class.java))
        }
        isMusicPlaying = !isMusicPlaying
        saveMusicState(isMusicPlaying)
        updateMusicIcon(musicToggleButton)
    }

    private fun updateMusicIcon(musicToggleButton: ImageView) {
        musicToggleButton.setImageResource(
            if (isMusicPlaying) R.drawable.volume_sound else R.drawable.volume_no_sound
        )
    }

    private fun saveMusicState(state: Boolean) {
        sharedPreferences.edit().putBoolean("music_state", state).apply()
    }
}