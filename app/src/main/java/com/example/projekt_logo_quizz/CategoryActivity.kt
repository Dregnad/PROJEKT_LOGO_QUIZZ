package com.example.projekt_logo_quizz

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class CategoryActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        findViewById<Button>(R.id.btnBack1).setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        // Pętla dodająca obsługę dla 10 poziomów
        for (i in 1..10) {
            val buttonId = resources.getIdentifier("level$i", "id", packageName)
            val button = findViewById<Button>(buttonId)

            button?.setOnClickListener {
                openQuizActivity(i) // Przechodzenie do QuizActivity z numerem poziomu
            }
        }
    }

    private fun openQuizActivity(level: Int) {
        val intent = Intent(this, LvlActivity::class.java)
        intent.putExtra("LEVEL", level) // Przekazanie poziomu do QuizActivity
        startActivity(intent)
    }
}
