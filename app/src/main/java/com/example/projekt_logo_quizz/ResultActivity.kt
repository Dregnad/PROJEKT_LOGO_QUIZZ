package com.example.projekt_logo_quizz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val score = intent.getIntExtra("score", 0)
        val scoreText = findViewById<TextView>(R.id.scoreText)
        val btnRetry = findViewById<Button>(R.id.btnRetry)
        val btnMenu = findViewById<Button>(R.id.btnMenu)


        scoreText.text = "Twój wynik: $score punktów"

        btnRetry.setOnClickListener {
            val intent = Intent(this, LvlActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnMenu.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
