package com.example.projekt_logo_quizz

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatisticsActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var pointsTextView: TextView // TextView do wyświetlania punktów
    private var showingHints: Boolean = false // Zmienna przechowująca stan wyświetlania wskazówek



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)


        sharedPref = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        pointsTextView = findViewById(R.id.pointsTextView)
        updatePointsDisplay()

        findViewById<ImageButton>(R.id.buttonArrow).setOnClickListener {
            // Przełączamy stan między punktami a wskazówkami
            showingHints = !showingHints
            // Zapiszemy nowy stan w SharedPreferences, jeśli chcemy go zachować
            sharedPref.edit().putBoolean("SHOWING_HINTS", showingHints).apply()
            // Zaktualizujemy wyświetlanie punktów i ikony
            updatePointsDisplay()
        }


        val totalPoints = sharedPref.getInt("TOTAL_POINTS", 0)
        val guessedLogos = sharedPref.getInt("TOTAL_GUESSED_LOGOS", 0)

        findViewById<TextView>(R.id.statsPoints).text = " $totalPoints"
        findViewById<TextView>(R.id.statsGuessed).text = " $guessedLogos"

        findViewById<ImageButton>(R.id.btnBackMenu).setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Funkcja do aktualizacji wyświetlania punktów i wskazówek
    private fun updatePointsDisplay() {
        val totalPoints = sharedPref.getInt("TOTAL_POINTS", 0)
        val hintPoints = sharedPref.getInt("HINT_POINTS", 0)
        println("Total Points: $totalPoints, Hint Points: $hintPoints") // Debugowanie

        if (showingHints) {
            findViewById<ImageButton>(R.id.buttonArrow).setImageResource(R.drawable.wskazowka_ico)
            pointsTextView.text = "$hintPoints x " // Wskazówki
        } else {
            findViewById<ImageButton>(R.id.buttonArrow).setImageResource(R.drawable.puzel)
            pointsTextView.text = "$totalPoints x " // Zwykłe punkty
        }
    }


}