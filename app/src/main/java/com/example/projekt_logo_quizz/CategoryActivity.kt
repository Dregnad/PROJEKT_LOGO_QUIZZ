package com.example.projekt_logo_quizz

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CategoryActivity : AppCompatActivity() {
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

    private val levelRequirements = mapOf(
        1 to 0,
        2 to 5,
        3 to 10,
        4 to 30,
        5 to 40,
        6 to 60,
        7 to 70,
        8 to 80,
        9 to 90,
        10 to 110
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

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

        val mode1Points = sharedPref.getInt("MODE1_POINTS", 0)

        for (level in 1..10) {
            val levelLayoutId = resources.getIdentifier("level$level", "id", packageName)
            val levelLayout = findViewById<RelativeLayout>(levelLayoutId)

            val lockLayerId = resources.getIdentifier("lockLayer$level", "id", packageName)
            val lockLayer = findViewById<RelativeLayout>(lockLayerId)

            val lockDescId = resources.getIdentifier("lockDesc$level", "id", packageName)
            val lockDesc = findViewById<TextView>(lockDescId)

            val completedLogosCount = sharedPref.getStringSet("GUESSED_LOGOS_LVL$level", mutableSetOf())?.size ?: 0
            val points = sharedPref.getInt("POINTS_LVL$level", 0)

            val pointsViewId = resources.getIdentifier("poziom${level}Punkty", "id", packageName)
            val logosViewId = resources.getIdentifier("poziom${level}Loga", "id", packageName)

            val pointsTextView = findViewById<TextView>(pointsViewId)
            val logosTextView = findViewById<TextView>(logosViewId)

            pointsTextView?.text = "Punkty: $points"
            logosTextView?.text = "Loga: $completedLogosCount/20"

            val requiredPoints = levelRequirements[level] ?: 0
            val remainingPoints = requiredPoints - mode1Points

            if (mode1Points  >= requiredPoints) {
                lockLayer?.visibility = View.GONE
                levelLayout?.visibility = View.VISIBLE
                levelLayout?.isEnabled = true
                levelLayout?.setOnClickListener {
                    musicService?.playClickSound()
                    openQuizActivity(level)
                }
            } else {
                lockLayer?.visibility = View.VISIBLE
                lockDesc?.text = "Uzyskaj jeszcze $remainingPoints punktów, aby odblokować."
                levelLayout?.visibility = View.GONE
                levelLayout?.isEnabled = false
                levelLayout?.setOnClickListener {
                    Toast.makeText(this, "Zdobądź $requiredPoints pkt, aby odblokować ten poziom!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<ImageButton>(R.id.btnBack1).setOnClickListener {
            musicService?.playClickSound()
            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun openQuizActivity(level: Int) {
        val intent = Intent(this, LvlActivity::class.java)
        intent.putExtra("LEVEL", level)
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
