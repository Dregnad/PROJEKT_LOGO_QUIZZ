package com.example.projekt_logo_quizz

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    //private lateinit var clickSound: MediaPlayer
    private lateinit var pointsTextView: TextView
    private var showingHints: Boolean = false
    private lateinit var gameSharedPref: SharedPreferences

    private var musicService: MusicService? = null
    private var isBound = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var musicVolumeSeekBar: SeekBar
    private lateinit var effectsVolumeSeekBar: SeekBar

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true

            val savedMusicVolume = sharedPreferences.getFloat("music_volume", 1.0f)
            musicService?.setMusicVolume(savedMusicVolume)
            val savedEffectsVolume = sharedPreferences.getFloat("effects_volume", 1.0f)
            musicService?.setEffectsVolume(savedEffectsVolume)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        musicVolumeSeekBar = findViewById(R.id.volumeSeekBar)
        effectsVolumeSeekBar = findViewById(R.id.effectsVolumeSeekBar)
        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        musicVolumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f
                musicService?.setMusicVolume(volume)
                saveMusicVolume(volume)
                if (volume == 0f) {
                    musicService?.pauseMusic()
                } else if (sharedPreferences.getBoolean("music_state", true)) {
                    musicService?.resumeMusic()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val savedMusicVolume = sharedPreferences.getFloat("music_volume", 1.0f)
        musicVolumeSeekBar.progress = (savedMusicVolume * 100).toInt()


        val savedEffectsVolume = sharedPreferences.getFloat("effects_volume", 1.0f)
        effectsVolumeSeekBar.progress = (savedEffectsVolume * 100).toInt()

        effectsVolumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f
                musicService?.setEffectsVolume(volume)
                saveEffectsVolume(volume)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })






        val backButton = findViewById<ImageButton>(R.id.backButton)
        val backImageButton = findViewById<ImageView>(R.id.btnBack2)
        val resetButton = findViewById<ImageButton>(R.id.btnImage1)

        gameSharedPref = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        pointsTextView = findViewById(R.id.pointsTextView)
        showingHints = gameSharedPref.getBoolean("SHOWING_HINTS", false)
        updatePointsDisplay()

        findViewById<ImageButton>(R.id.buttonArrow).setOnClickListener {
            musicService?.playClickSound()

            showingHints = !showingHints
            gameSharedPref.edit().putBoolean("SHOWING_HINTS", showingHints).apply()
            updatePointsDisplay()
        }

        backButton.setOnClickListener { finish()
            musicService?.playClickSound()

        }

        backImageButton.setOnClickListener {
            musicService?.playClickSound()

            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        resetButton.setOnClickListener {
            musicService?.playClickSound()

            resetAppProgrammatically()
        }




    }



    private fun saveMusicVolume(volume: Float) {
        sharedPreferences.edit().putFloat("music_volume", volume).apply()
    }

    private fun saveEffectsVolume(volume: Float) {
        sharedPreferences.edit().putFloat("effects_volume", volume).apply()
    }

    private fun resetAppProgrammatically() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.clearApplicationUserData()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun updatePointsDisplay() {
        val totalPoints = gameSharedPref.getInt("TOTAL_POINTS", 4)
        val hintPoints = gameSharedPref.getInt("HINT_POINTS", 5)

        if (showingHints) {
            findViewById<ImageButton>(R.id.buttonArrow).setImageResource(R.drawable.wskazowka_ico)
            pointsTextView.text = "$hintPoints x "
        } else {
            findViewById<ImageButton>(R.id.buttonArrow).setImageResource(R.drawable.puzel)
            pointsTextView.text = "$totalPoints x "
        }
    }
}