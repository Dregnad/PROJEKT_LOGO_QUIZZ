package com.example.projekt_logo_quizz

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AchievementsActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        sharedPref = getSharedPreferences("GamePrefs", MODE_PRIVATE)

        val achievementsList = listOf(
            Triple("Początek przygody", "Odgadnij 3 logotypy", "ACHIEVEMENT_3_LOGOS"),
            Triple("Ekspert pierwszego poziomu", "Ukończ pierwszy poziom", "ACHIEVEMENT_LVL1")
        )


        val layout = findViewById<LinearLayout>(R.id.achievementsContainer)

        for ((title, description, key) in achievementsList) {
            val textView = TextView(this).apply {
                text = "$title\n$description"
                textSize = 18f
                setPadding(16, 16, 16, 16)

                if (sharedPref.getBoolean(key, false)) {
                    setTextColor(Color.WHITE) // Zdobyte osiągnięcie
                } else {
                    setTextColor(Color.GRAY) // Niezdobyte
                }
            }
            layout.addView(textView)
        }

    }
}
