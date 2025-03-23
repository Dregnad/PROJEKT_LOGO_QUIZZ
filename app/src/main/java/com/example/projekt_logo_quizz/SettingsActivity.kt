package com.example.projekt_logo_quizz

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private var musicService: MusicService? = null
    private var isBound = false
    private lateinit var sharedPreferences: SharedPreferences



    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true

            // Po połączeniu ustawiamy głośność na tę zapisaną w SharedPreferences
            val savedVolume = sharedPreferences.getFloat("music_volume", 1.0f) // Domyślnie 100%
            musicService?.setVolume(savedVolume)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton = findViewById<Button>(R.id.backButton)
        val volumeSeekBar = findViewById<SeekBar>(R.id.volumeSeekBar)

        findViewById<Button>(R.id.btnBack2).setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        backButton.setOnClickListener { v: View? -> finish() }

        // Połączenie z MusicService
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        // Pobranie zapisanej wartości głośności i ustawienie suwaka
        val savedVolume = sharedPreferences.getFloat("music_volume", 1.0f)
        volumeSeekBar.progress = (savedVolume * 100).toInt()

        // Obsługa zmiany głośności
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f
                musicService?.setVolume(volume)
                saveVolume(volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun saveVolume(volume: Float) {
        val editor = sharedPreferences.edit()
        editor.putFloat("music_volume", volume)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}
