package com.example.projekt_logo_quizz

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.background_music)
            mediaPlayer?.isLooping = true

            // Pobranie zapisanej głośności i ustawienie jej
            val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val savedVolume = sharedPreferences.getFloat("music_volume", 1.0f)
            mediaPlayer?.setVolume(savedVolume, savedVolume)

            mediaPlayer?.start()
        }
        return START_STICKY
    }


    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }
}
