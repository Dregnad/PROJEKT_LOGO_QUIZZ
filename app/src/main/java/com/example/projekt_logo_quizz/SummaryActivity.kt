package com.example.projekt_logo_quizz

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class SummaryActivity : AppCompatActivity() {
    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true


            musicService?.playVictorySound()

            val completedLevel = intent.getIntExtra("COMPLETED_LEVEL", -1)
            val totalLevels = 10
            if (completedLevel == totalLevels) {
                musicService?.playLvl10Sound()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary_activity)

        // Połączenie z MusicService
        val serviceIntent = Intent(this, MusicService::class.java)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)

        val completedLevel = intent.getIntExtra("COMPLETED_LEVEL", -1)
        val totalLevels = 10

        if (completedLevel >= totalLevels) {
            findViewById<ImageButton>(R.id.btnNextLevel).visibility = View.GONE
        }

        findViewById<ImageButton>(R.id.btnBackToMenu).setOnClickListener {
            musicService?.playClickSound()
            musicService?.stopLvl10Sound()
            val intent = Intent(this, CategoryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        findViewById<ImageButton>(R.id.btnNextLevel).setOnClickListener {
            musicService?.playNextLvlSound()

            if (completedLevel != -1) {
                val nextLevel = completedLevel + 1
                val intent = Intent(this, LvlActivity::class.java)
                intent.putExtra("LEVEL", nextLevel)
                startActivity(intent)
            }

            musicService?.stopLvl10Sound()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}
