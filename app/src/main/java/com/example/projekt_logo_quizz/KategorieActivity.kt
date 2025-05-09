package com.example.projekt_logo_quizz

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class KategorieActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var pointsTextView: TextView
    private var showingHints: Boolean = false

    private val categoryRequirements = mapOf(
        1 to 0,
        2 to 4,
        3 to 8,
        4 to 12,
        5 to 16,
        6 to 20,
        7 to 24,
        8 to 28,
        9 to 32,
        10 to 36
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kategorie)

        sharedPref = getSharedPreferences("GamePrefs", MODE_PRIVATE)

        pointsTextView = findViewById(R.id.pointsTextView)
        updatePointsDisplay()

        findViewById<ImageButton>(R.id.buttonArrow).setOnClickListener {
            showingHints = !showingHints
            sharedPref.edit().putBoolean("SHOWING_HINTS", showingHints).apply()
            updatePointsDisplay()
        }

        val mode2Points = sharedPref.getInt("MODE2_POINTS", 0)

        for (category in 1..10) {
            val categoryLayoutId = resources.getIdentifier("kat$category", "id", packageName)
            val categoryLayout = findViewById<RelativeLayout>(categoryLayoutId)

            val lockLayerId = resources.getIdentifier("kat_lockLayer$category", "id", packageName)
            val lockLayer = findViewById<RelativeLayout>(lockLayerId)

            val lockDescId = resources.getIdentifier("kat_lockDesc$category", "id", packageName)
            val lockDesc = findViewById<TextView>(lockDescId)


            val completedLogosCount = sharedPref.getStringSet("GUESSED_LOGOS_KAT$category", mutableSetOf())?.size ?: 0
            val points = sharedPref.getInt("POINTS_KAT$category", 0)

            val pointsViewId = resources.getIdentifier("kat${category}Punkty", "id", packageName)
            val logosViewId = resources.getIdentifier("kat${category}Loga", "id", packageName)

            val pointsTextView = findViewById<TextView>(pointsViewId)
            val logosTextView = findViewById<TextView>(logosViewId)

            pointsTextView?.text = "Punkty: $points"
            logosTextView?.text = "Loga: $completedLogosCount/20"

            val requiredPoints = categoryRequirements[category] ?: 0
            val remainingPoints = requiredPoints - mode2Points

            if (mode2Points >= requiredPoints) {
                lockLayer?.visibility = View.GONE
                categoryLayout?.visibility = View.VISIBLE
                categoryLayout?.isEnabled = true
                categoryLayout?.setOnClickListener {
                    openQuizActivity(category)
                }
            } else {
                lockLayer?.visibility = View.VISIBLE
                lockDesc?.text = "Uzyskaj jeszcze $remainingPoints punktów, aby odblokować."
                categoryLayout?.visibility = View.GONE
                categoryLayout?.isEnabled = false
                categoryLayout?.setOnClickListener {
                    Toast.makeText(this, "Zdobądź $requiredPoints pkt, aby odblokować ten poziom!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<ImageButton>(R.id.btnBack1).setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun openQuizActivity(category: Int) {
        val intent = Intent(this, KatActivity::class.java)
        intent.putExtra("CATEGORY", category)
        startActivity(intent)
    }



    private fun updatePointsDisplay() {
        val totalPoints = sharedPref.getInt("TOTAL_POINTS", 0)
        val hintPoints = sharedPref.getInt("HINT_POINTS", 0)
        println("Total Points: $totalPoints, Hint Points: $hintPoints")

        if (showingHints) {
            findViewById<ImageButton>(R.id.buttonArrow).setImageResource(R.drawable.wskazowka_ico)
            pointsTextView.text = "$hintPoints x "
        } else {
            findViewById<ImageButton>(R.id.buttonArrow).setImageResource(R.drawable.puzel)
            pointsTextView.text = "$totalPoints x "
        }
    }
}
