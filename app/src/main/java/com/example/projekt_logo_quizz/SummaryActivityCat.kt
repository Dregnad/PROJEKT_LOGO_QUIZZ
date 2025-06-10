package com.example.projekt_logo_quizz

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class SummaryActivityCat : AppCompatActivity() {
    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true

            // Odtwórz dźwięk wygranej
            musicService?.playVictorySound()

            // Jeśli to ostatnia kategoria (10), odtwórz specjalny dźwięk
            val completedCategory = intent.getIntExtra("COMPLETED_CATEGORY", -1)
            if (completedCategory == 3) {
                musicService?.playLvl10Sound()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary_activity_cat)

        val serviceIntent = Intent(this, MusicService::class.java)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)

        val completedCategory = intent.getIntExtra("COMPLETED_CATEGORY", -1)

        // Ukryj przycisk dalej, jeśli to ostatnia kategoria
        if (completedCategory >= 3) {
            findViewById<ImageButton>(R.id.btnNextCategory).visibility = View.GONE
        }

        findViewById<ImageButton>(R.id.btnBackToMenu).setOnClickListener {
            musicService?.playClickSound()
            musicService?.stopLvl10Sound()
            val intent = Intent(this, KategorieActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        findViewById<ImageButton>(R.id.btnNextCategory).setOnClickListener {
            musicService?.playNextLvlSound()

            if (completedCategory != -1) {
                val nextCategory = completedCategory + 1
                val intent = Intent(this, KatActivity::class.java)
                intent.putExtra("CATEGORY", nextCategory)
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
