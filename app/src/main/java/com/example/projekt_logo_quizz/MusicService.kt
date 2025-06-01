package com.example.projekt_logo_quizz

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var clickSound: MediaPlayer? = null
    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isMusicEnabled = sharedPreferences.getBoolean("music_state", true)
        val savedMusicVolume = sharedPreferences.getFloat("music_volume", 1.0f)

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.ost_1)
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(savedMusicVolume, savedMusicVolume)
            if (isMusicEnabled) {
                mediaPlayer?.start()
            }
        }

        if (clickSound == null) {
            clickSound = MediaPlayer.create(this, R.raw.click_literki)
            val savedEffectsVolume = sharedPreferences.getFloat("effects_volume", 1.0f)
            clickSound?.setVolume(savedEffectsVolume, savedEffectsVolume)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        clickSound?.release()
        clickSound = null
        super.onDestroy()
    }

    fun setMusicVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    fun setEffectsVolume(volume: Float) {
        clickSound?.setVolume(volume, volume)
    }

    fun playClickSound() {
        clickSound?.start()
    }

    // Nowe metody do kontroli muzyki
    fun pauseMusic() {
        mediaPlayer?.pause()
    }

    fun resumeMusic() {
        mediaPlayer?.start()
    }

    fun isMusicPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
}