package com.example.projekt_logo_quizz

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Opóźnienie 2 sekundy (możesz zmienić)
        Handler().postDelayed({
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }, 2000)
    }
}
