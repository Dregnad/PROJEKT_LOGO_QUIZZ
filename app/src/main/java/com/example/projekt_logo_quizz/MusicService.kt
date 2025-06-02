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
    private var dobrySound: MediaPlayer? = null
    private var zlySound: MediaPlayer? = null
    private var victorySound: MediaPlayer? = null
    private var lvl10Sound: MediaPlayer? = null
    private var nextLvlSound: MediaPlayer? = null
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
        val savedEffectsVolume = sharedPreferences.getFloat("effects_volume", 1.0f)

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
            clickSound?.setVolume(savedEffectsVolume, savedEffectsVolume)
        }

        if (dobrySound == null) {
            dobrySound = MediaPlayer.create(this, R.raw.dobry)
            dobrySound?.setVolume(savedEffectsVolume, savedEffectsVolume)
        }

        if (zlySound == null) {
            zlySound = MediaPlayer.create(this, R.raw.zly)
            zlySound?.setVolume(savedEffectsVolume, savedEffectsVolume)
        }

        if (victorySound == null) {
            victorySound = MediaPlayer.create(this, R.raw.victory)
            victorySound?.setVolume(savedEffectsVolume, savedEffectsVolume)
        }

        if (lvl10Sound == null) {
            lvl10Sound = MediaPlayer.create(this, R.raw.lvl10)
            lvl10Sound?.setVolume(savedEffectsVolume, savedEffectsVolume)
        }

        if (nextLvlSound == null) {
            nextLvlSound = MediaPlayer.create(this, R.raw.next_lvl)
            nextLvlSound?.setVolume(savedEffectsVolume, savedEffectsVolume)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        clickSound?.release()
        clickSound = null
        dobrySound?.release()
        dobrySound = null
        zlySound?.release()
        zlySound = null
        victorySound?.release()
        victorySound = null
        lvl10Sound?.release()
        lvl10Sound = null
        nextLvlSound?.release()
        nextLvlSound = null
        super.onDestroy()
    }

    fun setMusicVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    fun setEffectsVolume(volume: Float) {
        clickSound?.setVolume(volume, volume)
        dobrySound?.setVolume(volume, volume)
        zlySound?.setVolume(volume, volume)
        victorySound?.setVolume(volume, volume)
        lvl10Sound?.setVolume(volume, volume)
        nextLvlSound?.setVolume(volume, volume)
    }

    fun playClickSound() {
        clickSound?.start()
    }

    fun playDobrySound() {
        dobrySound?.start()
    }

    fun playZlySound() {
        zlySound?.start()
    }

    fun playVictorySound() {
        victorySound?.start()
    }

    fun playLvl10Sound() {
        lvl10Sound?.start()
    }

    fun playNextLvlSound() {
        nextLvlSound?.start()
    }

    fun stopLvl10Sound() {
        lvl10Sound?.let {
            if (it.isPlaying) {
                it.stop()
                it.prepare() // Przygotowanie do ponownego odtwarzania
            }
        }
    }

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