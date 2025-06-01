package com.example.projekt_logo_quizz

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var isMusicPlaying = true
    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            // Zsynchronizuj stan przycisku z rzeczywistym stanem muzyki
            isMusicPlaying = musicService?.isMusicPlaying() ?: true
            updateMusicIcon(findViewById(R.id.musicToggleButton))
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Start serwisu
        startService(Intent(this, MusicService::class.java))
        // Połącz się z serwisem
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        val settingsButton = findViewById<ImageView>(R.id.settingsButton)
        val categoryButton = findViewById<Button>(R.id.categoryButton)

        val btnStatistics = findViewById<ImageView>(R.id.btnStatistics)
        val exitButton = findViewById<Button>(R.id.button_exit)

        val btnAchievements = findViewById<ImageView>(R.id.btnAchievements)
        val kategorieButton = findViewById<Button>(R.id.button_categories)
        val musicToggleButton = findViewById<ImageView>(R.id.musicToggleButton)
        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        isMusicPlaying = sharedPreferences.getBoolean("music_state", true)
        updateMusicIcon(musicToggleButton)

        musicToggleButton.setOnClickListener {
            musicService?.playClickSound()
            animateClick(it)
            toggleMusic(musicToggleButton)
        }



        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        isMusicPlaying = sharedPreferences.getBoolean("music_state", true)
        updateMusicIcon(musicToggleButton)

        settingsButton.setOnClickListener {
            musicService?.playClickSound()
            animateClick(it)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        categoryButton.setOnClickListener {
            musicService?.playClickSound()
            animateClick(it)
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        kategorieButton.setOnClickListener {
            musicService?.playClickSound()
            animateClick(it)
            startActivity(Intent(this, KategorieActivity::class.java))
        }

        btnStatistics.setOnClickListener {
            musicService?.playClickSound()
            animateClick(it)
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        btnAchievements.setOnClickListener {
            musicService?.playClickSound()
            animateClick(it)
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

        exitButton.setOnClickListener {
            musicService?.playClickSound()
            animateClick(it)
            finishAffinity()
            System.exit(0)
        }

        musicToggleButton.setOnClickListener {
            musicService?.playClickSound()
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
            musicService?.pauseMusic()
        } else {
            musicService?.resumeMusic()
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

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun saveMusicState(state: Boolean) {
        sharedPreferences.edit().putBoolean("music_state", state).apply()
    }
}