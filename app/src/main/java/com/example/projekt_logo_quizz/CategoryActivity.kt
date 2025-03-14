package com.example.projekt_logo_quizz

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity





class CategoryActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener { v: View? -> finish() }

        val flagaButton = findViewById<ImageView>(R.id.flagaButton)
        val autaButton = findViewById<ImageView>(R.id.autaButton)

        flagaButton.setOnClickListener {
            openMainActivity("flaga")
        }

        autaButton.setOnClickListener {
            openMainActivity("auta")
        }
    }

    private fun openMainActivity(category: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("CATEGORY", category) // Przekazanie kategorii do MainActivity
        startActivity(intent)
    }
}
