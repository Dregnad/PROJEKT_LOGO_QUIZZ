package com.example.projekt_logo_quizz

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SummaryActivity : AppCompatActivity() {
    private lateinit var clickSound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary_activity)

        clickSound = MediaPlayer.create(this, R.raw.click_literki)

        val completedLevel = intent.getIntExtra("COMPLETED_LEVEL", -1)
        val completedCategory = intent.getIntExtra("COMPLETED_CATEGORY", -1)
        val totalLevels = 5 // Zmień na rzeczywistą liczbę poziomów/kategorii

        if (completedLevel >= totalLevels || completedCategory >= totalLevels) {
            findViewById<Button>(R.id.btnNextLevel).visibility = View.GONE
        }

        findViewById<Button>(R.id.btnBackToMenu).setOnClickListener {
            clickSound.start()
            val intent = Intent(this, CategoryActivity::class.java) // lub KategorieActivity
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnNextLevel).setOnClickListener {
            clickSound.start()
            if (completedLevel != -1) {
                val nextLevel = completedLevel + 1
                val intent = Intent(this, LvlActivity::class.java)
                intent.putExtra("LEVEL", nextLevel)
                startActivity(intent)
            } else if (completedCategory != -1) {
                val nextCategory = completedCategory + 1
                val intent = Intent(this, KatActivity::class.java)
                intent.putExtra("CATEGORY", nextCategory)
                startActivity(intent)
            }
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clickSound.release()
    }
}