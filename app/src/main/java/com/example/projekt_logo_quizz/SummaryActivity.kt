package com.example.projekt_logo_quizz

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SummaryActivity : AppCompatActivity() {

    private lateinit var totalPointsTextView: TextView
    private lateinit var backButton: Button
    private lateinit var sharedPref: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary_activity)

        totalPointsTextView = findViewById(R.id.summaryTextView)
        backButton = findViewById(R.id.backButton)

        sharedPref = getSharedPreferences("GamePrefs", MODE_PRIVATE)

        val totalPoints = sharedPref.getInt("TOTAL_POINTS", 0)
        totalPointsTextView.text = "Łączna liczba punktów: $totalPoints"

        backButton.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
