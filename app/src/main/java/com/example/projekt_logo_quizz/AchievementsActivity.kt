package com.example.projekt_logo_quizz

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.RelativeLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class AchievementsActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var pointsTextView: TextView

    private var showingHints: Boolean = false

    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)
        // Połączenie z MusicService
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        sharedPref = getSharedPreferences("GamePrefs", MODE_PRIVATE)

        pointsTextView = findViewById(R.id.pointsTextView)

        updatePointsDisplay()

        findViewById<ImageButton>(R.id.buttonArrow).setOnClickListener {
            musicService?.playClickSound()
            showingHints = !showingHints
            sharedPref.edit().putBoolean("SHOWING_HINTS", showingHints).apply()
            updatePointsDisplay()
        }

        val achievementsList = listOf(



            // Odgadywanie logotypów
            Triple("Początek przygody", "Odgadnij 5 logotypy", "ACHIEVEMENT_5_LOGOS"),
            Triple("Entuzjasta logotypów", "Odgadnij 40 logotypów", "ACHIEVEMENT_40_LOGOS"),
            Triple("Mistrz logotypów", "Odgadnij 80 logotypów", "ACHIEVEMENT_80_LOGOS"),
            Triple("Ekspert", "Odgadnij 200 logotypów", "ACHIEVEMENT_200_LOGOS"),

            // Ukończenie poziomów
            Triple("Ekspert pierwszego poziomu", "Ukończ pierwszy poziom", "ACHIEVEMENT_LVL1"),
            Triple("Połowa drogi", "Ukończ 5 poziomów", "ACHIEVEMENT_LVL5"),
            Triple("Ekspert poziomów", "Ukończ 7 poziomów", "ACHIEVEMENT_LVL7"),
            Triple("Niepowstrzymany", "Ukończ 10 poziom", "ACHIEVEMENT_LVL10")

        )


        val layout = findViewById<LinearLayout>(R.id.achievementsContainer)

        for ((title, description, key) in achievementsList) {
            val achievementLayout = RelativeLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    300
                ).apply {
                    setMargins(0, 60, 0, 40)
                }
                setPadding(16, 16, 16, 16)
                background = resources.getDrawable(R.drawable.osiagniecie_tlo, null)

                val imageView = ImageView(this@AchievementsActivity).apply {
                    val imageRes = if (sharedPref.getBoolean(key, false)) {
                        R.drawable.puchar
                    } else {
                        R.drawable.klodka
                    }
                    setImageResource(imageRes)
                    layoutParams = RelativeLayout.LayoutParams(200, 200).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_END)
                        addRule(RelativeLayout.CENTER_VERTICAL)
                    }
                }

                // Tekst osiągnięcia
                val textView = TextView(this@AchievementsActivity).apply {
                    text = "$title\n$description"
                    textSize = 18f
                    setTextColor(if (sharedPref.getBoolean(key, false)) Color.WHITE else Color.BLACK)
                    layoutParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_START)
                        addRule(RelativeLayout.ALIGN_PARENT_TOP)
                        setMargins(16, 50, 0, 50)
                    }
                }

                addView(textView)
                addView(imageView)
            }

            layout.addView(achievementLayout)
        }

        val btnBackMenu = findViewById<ImageButton>(R.id.btnBackMenu)
        btnBackMenu.setOnClickListener {
            musicService?.playClickSound()
            onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun updatePointsDisplay() {
        val totalPoints = sharedPref.getInt("TOTAL_POINTS", 4)
        val hintPoints = sharedPref.getInt("HINT_POINTS", 5)
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
