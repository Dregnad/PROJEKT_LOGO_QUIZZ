package com.example.projekt_logo_quizz

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatisticsActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        sharedPref = getSharedPreferences("GamePrefs", MODE_PRIVATE)

        val totalPoints = sharedPref.getInt("TOTAL_POINTS", 0)
        val guessedLogos = sharedPref.getInt("TOTAL_GUESSED_LOGOS", 0)

        findViewById<TextView>(R.id.statsPoints).text = "Zdobyte punkty: $totalPoints"
        findViewById<TextView>(R.id.statsGuessed).text = "Zgadnięte logotypy: $guessedLogos"

        findViewById<Button>(R.id.btnBackMenu).setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}