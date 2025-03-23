package com.example.projekt_logo_quizz

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout
import com.squareup.picasso.Picasso
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class LvlActivity : AppCompatActivity() {

    private lateinit var logoImageView: ImageView
    private lateinit var answerContainer: LinearLayout
    private lateinit var lettersContainer: FlexboxLayout
    private lateinit var resetButton: Button
    private lateinit var letterButtons: MutableList<Button>
    private lateinit var answerSlots: MutableList<TextView>
    private lateinit var pointsTextView: TextView
    private var correctAnswer: String = ""
    private var currentIndex: Int = 0
    private var logosList: List<Logo> = listOf()
    private lateinit var sharedPref: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lvl_quiz)

        logoImageView = findViewById(R.id.logoImageView)
        answerContainer = findViewById(R.id.answerContainer)
        lettersContainer = findViewById(R.id.lettersContainer)
        resetButton = findViewById(R.id.resetButton)
        pointsTextView = findViewById(R.id.pointsTextView)
        findViewById<Button>(R.id.btnNext).setOnClickListener { nextLogo() }
        findViewById<Button>(R.id.btnPrev).setOnClickListener { prevLogo() }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        letterButtons = mutableListOf()
        answerSlots = mutableListOf()
        sharedPref = getSharedPreferences("GamePrefs", MODE_PRIVATE)

        val level = intent.getIntExtra("LEVEL", -1)
        if (level == -1) {
            Toast.makeText(this, "Błąd ładowania poziomu!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (isLevelAlreadyCompleted(level)) {
            goToSummary()
            return
        }

        resetButton.setOnClickListener { resetGame() }

        loadLogosForLevel(level)
        updateTotalPointsDisplay()
    }

    private fun loadLogosForLevel(level: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = supabase.from("poziom$level")
                    .select()
                    .decodeList<Logo>()

                if (response.isEmpty()) return@launch

                logosList = response.sortedBy { it.id }

                runOnUiThread {
                    currentIndex = findFirstUnsolvedLogo()
                    loadQuestion()
                }
            } catch (e: Exception) {
                Log.e("LvlActivity", "Błąd pobierania logotypów: ${e.message}")
            }
        }
    }





    private fun generateRandomLetters(count: Int): List<Char> {
        val alphabet = ('A'..'Z').toList()
        return List(count) { alphabet.random() }
    }


    private fun findFirstUnsolvedLogo(): Int {
        val level = intent.getIntExtra("LEVEL", -1) // ✅ Pobranie poziomu
        if (level == -1) return logosList.size // ✅ Sprawdzenie błędu

        for (i in logosList.indices) {
            if (!isLogoAlreadyGuessed(logosList[i].id, level)) {
                return i
            }
        }
        return logosList.size
    }



    private fun loadQuestion() {
        if (currentIndex >= logosList.size) {
            markLevelAsCompleted(intent.getIntExtra("LEVEL", 1))
            goToSummary()
            return
        }

        val logo = logosList[currentIndex]
        correctAnswer = logo.name.uppercase()
        val level = intent.getIntExtra("LEVEL", -1)

        Picasso.get().load(logo.image).into(logoImageView)

        if (isLogoAlreadyGuessed(logo.id, level)) {
            setupGame(correctAnswer) // ✅ Pokazuje poprawną odpowiedź
            disableInput() // ✅ Blokuje wpisywanie liter
        } else {
            setupGame(correctAnswer) // Normalnie uruchamia grę
        }

        updateNavButtons()
    }





    private fun updateNavButtons() {
        findViewById<Button>(R.id.btnNext).isEnabled = currentIndex < logosList.size - 1
        findViewById<Button>(R.id.btnPrev).isEnabled = currentIndex > 0
    }

    private fun disableInput() {
        letterButtons.forEach { it.isEnabled = false }
        answerSlots.forEachIndexed { index, slot ->
            slot.text = correctAnswer.getOrNull(index)?.toString() ?: "_"
        }
    }


    private fun enableInput() {
        letterButtons.forEach { it.isEnabled = true }
        answerSlots.forEach { it.text = "_" }
    }

    private fun checkIfLevelCompleted(): Boolean {
        val level = intent.getIntExtra("LEVEL", -1) // ✅ Pobranie poziomu
        if (level == -1) {
            Toast.makeText(this, "Błąd poziomu!", Toast.LENGTH_SHORT).show()
            return false
        }

        return logosList.all { isLogoAlreadyGuessed(it.id, level) } // ✅ Teraz przekazujemy `level`
    }

    private fun prevLogo() {
        if (currentIndex > 0) {
            currentIndex--
            loadQuestion()
        }
        updateNavButtons() // ✅ Aktualizuj przyciski
    }


    private fun nextLogo() {
        if (currentIndex < logosList.size - 1) {
            currentIndex++
            loadQuestion()
        }
    }

    private fun checkAchievements() {
        val guessedLogos = sharedPref.getInt("TOTAL_GUESSED_LOGOS", 0)
        val completedLevels = sharedPref.getStringSet("COMPLETED_LEVELS", mutableSetOf()) ?: mutableSetOf()

        if (guessedLogos >= 3 && !sharedPref.getBoolean("ACHIEVEMENT_3_LOGOS", false)) {
            sharedPref.edit().putBoolean("ACHIEVEMENT_3_LOGOS", true).apply()
            Toast.makeText(this, "Osiągnięcie odblokowane: Początek przygody!", Toast.LENGTH_LONG).show()
        }

        if (completedLevels.contains("1") && !sharedPref.getBoolean("ACHIEVEMENT_LVL1", false)) {
            sharedPref.edit().putBoolean("ACHIEVEMENT_LVL1", true).apply()
            Toast.makeText(this, "Osiągnięcie odblokowane: Ekspert pierwszego poziomu!", Toast.LENGTH_LONG).show()
        }
    }



    private fun checkAnswer() {
        val userAnswer = answerSlots.joinToString("") { it.text.toString() }

        if (!userAnswer.contains("_")) {
            if (userAnswer.equals(correctAnswer, ignoreCase = true)) {
                Toast.makeText(this, "Brawo! Dobra odpowiedź!", Toast.LENGTH_SHORT).show()

                val level = intent.getIntExtra("LEVEL", -1)
                if (level == -1) {
                    Toast.makeText(this, "Błąd poziomu!", Toast.LENGTH_SHORT).show()
                    return
                }

                if (!isLogoAlreadyGuessed(logosList[currentIndex].id, level)) {
                    saveTotalPoints(logosList[currentIndex].points)
                    incrementGuessedLogos() // Aktualizacja liczby odgadniętych logotypów
                    markLogoAsGuessed(logosList[currentIndex].id, level)
                }

                currentIndex = findFirstUnsolvedLogo()
                loadQuestion()
            } else {
                Toast.makeText(this, "Błędna odpowiedź! Spróbuj ponownie!", Toast.LENGTH_SHORT).show()

                answerContainer.postDelayed({
                    resetGame()
                }, 1000)
            }
        }
    }


    private fun incrementGuessedLogos() {
        val guessedLogos = sharedPref.getInt("TOTAL_GUESSED_LOGOS", 0) + 1
        sharedPref.edit().putInt("TOTAL_GUESSED_LOGOS", guessedLogos).apply()
        checkAchievements()
    }



    private fun resetGame() {
        setupGame(correctAnswer) // Restartuje bieżącą grę
    }

    private fun updateTotalPointsDisplay() {
        val totalPoints = sharedPref.getInt("TOTAL_POINTS", 0)
        pointsTextView.text = "Punkty: $totalPoints"
    }

    private fun setupGame(correctAnswer: String) {
        answerContainer.removeAllViews()
        lettersContainer.removeAllViews()
        letterButtons.clear()
        answerSlots.clear()

        for (i in correctAnswer.indices) {
            val textView = TextView(this).apply {
                text = "_"
                textSize = 32f
                setPadding(16, 16, 16, 16)
                setTextColor(Color.BLACK)
            }
            answerSlots.add(textView)
            answerContainer.addView(textView)
        }

        val shuffledLetters: List<Char> = (correctAnswer.toList() + generateRandomLetters(correctAnswer.length)).shuffled()

        for (letter in shuffledLetters) {
            val button = Button(this).apply {
                text = letter.toString()
                textSize = 24f
                setOnClickListener { onLetterClicked(this) }
            }
            letterButtons.add(button)
            lettersContainer.addView(button)
        }
    }

    private fun onLetterClicked(button: Button) {
        for (slot in answerSlots) {
            if (slot.text == "_") {
                slot.text = button.text
                button.isEnabled = false
                checkAnswer()
                break
            }
        }
    }


    private fun saveTotalPoints(points: Int) {
        val totalPoints = sharedPref.getInt("TOTAL_POINTS", 0) + points
        sharedPref.edit().putInt("TOTAL_POINTS", totalPoints).apply()
        updateTotalPointsDisplay()
    }


    private fun markLogoAsGuessed(logoId: Int, level: Int) {
        val guessedLogos = sharedPref.getStringSet("GUESSED_LOGOS_LVL$level", mutableSetOf()) ?: mutableSetOf()
        guessedLogos.add(logoId.toString())
        sharedPref.edit().putStringSet("GUESSED_LOGOS_LVL$level", guessedLogos).apply()
    }


    private fun isLogoAlreadyGuessed(logoId: Int, level: Int): Boolean {
        val guessedLogos = sharedPref.getStringSet("GUESSED_LOGOS_LVL$level", mutableSetOf()) ?: mutableSetOf()
        return guessedLogos.contains(logoId.toString())
    }


    private fun markLevelAsCompleted(level: Int) {
        val completedLevels = sharedPref.getStringSet("COMPLETED_LEVELS", mutableSetOf()) ?: mutableSetOf()
        completedLevels.add(level.toString())
        sharedPref.edit().putStringSet("COMPLETED_LEVELS", completedLevels).apply()
        checkAchievements()
    }

    private fun isLevelAlreadyCompleted(level: Int): Boolean {
        val completedLevels = sharedPref.getStringSet("COMPLETED_LEVELS", mutableSetOf()) ?: mutableSetOf()
        return completedLevels.contains(level.toString())
    }

    private fun goToSummary() {
        startActivity(Intent(this, SummaryActivity::class.java))
        finish()
    }
}

@Serializable
data class Logo(
    val id: Int,
    val name: String,
    val image: String,
    val points: Int
)
