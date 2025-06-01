package com.example.projekt_logo_quizz

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.squareup.picasso.Picasso
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class KatActivity : AppCompatActivity() {

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

    private lateinit var logoImageView: ImageView
    private lateinit var answerContainer: FlexboxLayout
    private lateinit var lettersContainer: FlexboxLayout
    private lateinit var hintP1TextView: TextView
    private lateinit var hintP2TextView: TextView
    private lateinit var letterButtons: MutableList<Button>
    private lateinit var answerSlots: MutableList<TextView>
    private lateinit var pointsTextView: TextView
    private lateinit var correctSound: MediaPlayer
    private lateinit var wrongSound: MediaPlayer
    private var correctAnswer: String = ""
    private var currentIndex: Int = 0
    private var logosList: List<Logo> = listOf()
    private var showingHints = false
    private var hintPoints = 0
    private lateinit var sharedPref: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kat_quizz)

        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        sharedPref = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        hintPoints = sharedPref.getInt("HINT_POINTS", 0)

        logoImageView = findViewById(R.id.logoImageView)
        answerContainer = findViewById(R.id.answerContainer)
        lettersContainer = findViewById(R.id.lettersContainer)
        pointsTextView = findViewById(R.id.pointsTextView)
        hintP1TextView = findViewById(R.id.hintP1)
        hintP2TextView = findViewById(R.id.hintP2)

        correctSound = MediaPlayer.create(this, R.raw.odgadniencie_loga)
        wrongSound = MediaPlayer.create(this, R.raw.zle_logo)

        findViewById<ImageButton>(R.id.btnNext).setOnClickListener {
            musicService?.playClickSound()
            nextLogo()
        }
        findViewById<ImageButton>(R.id.btnPrev).setOnClickListener {
            musicService?.playClickSound()
            prevLogo()
        }
        findViewById<ImageButton>(R.id.btnBack3).setOnClickListener {
            musicService?.playClickSound()
            val intent = Intent(this, KategorieActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        val buttonArrow = findViewById<ImageButton>(R.id.buttonArrow)
        buttonArrow.setOnClickListener {
            showingHints = !showingHints
            updatePointsDisplay()
        }

        findViewById<ImageButton>(R.id.podpowiedz1).setOnClickListener {
            musicService?.playClickSound()
            val level = intent.getIntExtra("CATEGORY", -1)
            if (isLogoAlreadyGuessed(logosList[currentIndex].id, level)) {
                Toast.makeText(this, "To logo już zostało odgadnięte!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hintPoints > 0) {
                revealRandomLetter()
                hintPoints--
                sharedPref.edit().putInt("HINT_POINTS", hintPoints).apply()
                val usedHints = sharedPref.getInt("USED_HINTS", 0)
                sharedPref.edit().putInt("USED_HINTS", usedHints + 1).apply()
                updatePointsDisplay()
            } else {
                Toast.makeText(this, "Brak dostępnych wskazówek!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageButton>(R.id.podpowiedz2).setOnClickListener {
            musicService?.playClickSound()
            val level = intent.getIntExtra("CATEGORY", -1)
            if (isLogoAlreadyGuessed(logosList[currentIndex].id, level)) {
                Toast.makeText(this, "To logo już zostało odgadnięte!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hintPoints > 0) {
                removeOneWrongLetter()
                hintPoints--
                sharedPref.edit().putInt("HINT_POINTS", hintPoints).apply()
                val usedHints = sharedPref.getInt("USED_HINTS", 0)
                sharedPref.edit().putInt("USED_HINTS", usedHints + 1).apply()
                updatePointsDisplay()
            } else {
                Toast.makeText(this, "Brak dostępnych wskazówek!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageButton>(R.id.podpowiedz3).setOnClickListener {
            musicService?.playClickSound()
            val level = intent.getIntExtra("CATEGORY", -1)
            if (isLogoAlreadyGuessed(logosList[currentIndex].id, level)) {
                Toast.makeText(this, "To logo już zostało odgadnięte!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hintPoints >= 2) {
                hintPoints -= 2
                sharedPref.edit().putInt("HINT_POINTS", hintPoints).apply()
                val usedHints = sharedPref.getInt("USED_HINTS", 0)
                sharedPref.edit().putInt("USED_HINTS", usedHints + 2).apply()
                updatePointsDisplay()
                skipCurrentLogo()
            } else {
                Toast.makeText(this, "Potrzebujesz 2 punkty wskazówek!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageButton>(R.id.podpowiedz4).setOnClickListener {
            musicService?.playClickSound()
            showHint(1)
        }

        findViewById<ImageButton>(R.id.podpowiedz5).setOnClickListener {
            musicService?.playClickSound()
            showHint(2)
        }

        findViewById<ImageButton>(R.id.podpowiedz6).setOnClickListener {
            musicService?.playClickSound()
            val level = intent.getIntExtra("CATEGORY", -1)
            if (isLogoAlreadyGuessed(logosList[currentIndex].id, level)) {
                Toast.makeText(this, "To logo już zostało odgadnięte!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hintPoints > 0) {
                revealFirstAndLastLetter()
                hintPoints--
                sharedPref.edit().putInt("HINT_POINTS", hintPoints).apply()
                val usedHints = sharedPref.getInt("USED_HINTS", 0)
                sharedPref.edit().putInt("USED_HINTS", usedHints + 1).apply()
                updatePointsDisplay()
            } else {
                Toast.makeText(this, "Brak dostępnych wskazówek!", Toast.LENGTH_SHORT).show()
            }
        }

        letterButtons = mutableListOf()
        answerSlots = mutableListOf()

        val level = intent.getIntExtra("CATEGORY", -1)
        if (level == -1) {
            Toast.makeText(this, "Błąd ładowania poziomu!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (isLevelAlreadyCompleted(level)) {
            goToSummary()
            return
        }

        loadLogosForLevel(level)
        updatePointsDisplay()
    }

    private fun loadLogosForLevel(level: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = supabase.from("kategoria$level")
                    .select()
                    .decodeList<Logo>()

                if (response.isEmpty()) return@launch

                logosList = response.sortedBy { it.id }

                runOnUiThread {
                    currentIndex = findFirstUnsolvedLogo()
                    loadQuestion()
                }
            } catch (e: Exception) {
                Log.e("KatActivity", "Błąd pobierania logotypów: ${e.message}")
            }
        }
    }

    private fun generateRandomLetters(count: Int): List<Char> {
        val alphabet = ('A'..'Z').toList()
        return List(count) { alphabet.random() }
    }

    private fun findFirstUnsolvedLogo(): Int {
        val level = intent.getIntExtra("CATEGORY", -1)
        if (level == -1) return logosList.size

        for (i in logosList.indices) {
            if (!isLogoAlreadyGuessed(logosList[i].id, level)) {
                return i
            }
        }
        return logosList.size
    }

    private fun loadQuestion() {
        if (currentIndex >= logosList.size) {
            markLevelAsCompleted(intent.getIntExtra("CATEGORY", 1))
            goToSummary()
            return
        }

        val logo = logosList[currentIndex]
        correctAnswer = logo.name.uppercase()
        val level = intent.getIntExtra("CATEGORY", -1)

        Picasso.get().load(logo.image).into(logoImageView)

        if (isLogoAlreadyGuessed(logo.id, level)) {
            setupGame(correctAnswer)
            disableInput()
        } else {
            setupGame(correctAnswer)
        }

        updateNavButtons()
    }

    private fun updateNavButtons() {
        findViewById<ImageButton>(R.id.btnNext).isEnabled = currentIndex < logosList.size - 1
        findViewById<ImageButton>(R.id.btnPrev).isEnabled = currentIndex > 0
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
        val level = intent.getIntExtra("CATEGORY", -1)
        if (level == -1) {
            Toast.makeText(this, "Błąd poziomu!", Toast.LENGTH_SHORT).show()
            return false
        }

        return logosList.all { isLogoAlreadyGuessed(it.id, level) }
    }

    private fun prevLogo() {
        if (currentIndex > 0) {
            currentIndex--
            loadQuestion()
        }
        updateNavButtons()
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
                correctSound.start()

                val level = intent.getIntExtra("CATEGORY", -1)
                if (level == -1) {
                    Toast.makeText(this, "Błąd poziomu!", Toast.LENGTH_SHORT).show()
                    return
                }

                if (!isLogoAlreadyGuessed(logosList[currentIndex].id, level)) {
                    saveTotalPoints(logosList[currentIndex].points)
                    incrementGuessedLogos()
                    markLogoAsGuessed(logosList[currentIndex].id, level)
                }

                if (checkIfLevelCompleted()) {
                    markLevelAsCompleted(level)
                    goToSummary()
                } else {
                    currentIndex = findFirstUnsolvedLogo()
                    loadQuestion()
                }
            } else {
                Toast.makeText(this, "Błędna odpowiedź! Spróbuj ponownie!", Toast.LENGTH_SHORT).show()
                wrongSound.start()

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
        setupGame(correctAnswer)
    }

    private fun updatePointsDisplay() {
        sharedPref.edit().putBoolean("SHOWING_HINTS", showingHints).apply()
        if (showingHints) {
            findViewById<ImageButton>(R.id.buttonArrow).setImageResource(R.drawable.wskazowka_ico)
            pointsTextView.text = "$hintPoints x "
        } else {
            findViewById<ImageButton>(R.id.buttonArrow).setImageResource(R.drawable.puzel)
            val totalPoints = sharedPref.getInt("TOTAL_POINTS", 0)
            pointsTextView.text = "$totalPoints x "
        }
    }

    private fun revealRandomLetter() {
        val emptySlots = answerSlots.withIndex().filter { it.value.text == "_" || it.value.text.isBlank() }
        if (emptySlots.isNotEmpty()) {
            val randomSlot = emptySlots.random()
            val index = randomSlot.index
            val correctChar = correctAnswer[index].toString()

            answerSlots[index].text = correctChar

            val matchingButton = letterButtons.find { it.text == correctChar && it.visibility == View.VISIBLE }
            matchingButton?.visibility = View.INVISIBLE
        }
        checkAnswer()
    }

    private fun revealFirstAndLastLetter() {
        val firstIndex = 0
        val lastIndex = correctAnswer.length - 1

        if (answerSlots[firstIndex].text == "_" || answerSlots[firstIndex].text.isBlank()) {
            val firstChar = correctAnswer[firstIndex].toString()
            answerSlots[firstIndex].text = firstChar
            val firstButton = letterButtons.find { it.text == firstChar && it.visibility == View.VISIBLE }
            firstButton?.visibility = View.INVISIBLE
        }

        if (answerSlots[lastIndex].text == "_" || answerSlots[lastIndex].text.isBlank()) {
            val lastChar = correctAnswer[lastIndex].toString()
            answerSlots[lastIndex].text = lastChar
            val lastButton = letterButtons.find { it.text == lastChar && it.visibility == View.VISIBLE }
            lastButton?.visibility = View.INVISIBLE
        }

        checkAnswer()
    }

    private fun removeOneWrongLetter() {
        val correctLetters = correctAnswer.toSet()
        val wrongButtons = letterButtons.filter {
            it.visibility == View.VISIBLE && it.text[0] !in correctLetters
        }
        if (wrongButtons.isNotEmpty()) {
            wrongButtons.random().visibility = View.INVISIBLE
        }
    }

    private fun skipCurrentLogo() {
        val level = intent.getIntExtra("CATEGORY", -1)
        if (level == -1) return

        val logo = logosList[currentIndex]
        if (!isLogoAlreadyGuessed(logo.id, level)) {
            saveTotalPoints(logo.points)
            incrementGuessedLogos()
            markLogoAsGuessed(logo.id, level)
        }

        currentIndex = findFirstUnsolvedLogo()
        loadQuestion()
    }

    private fun showHint(hintNumber: Int) {
        val level = intent.getIntExtra("CATEGORY", -1)
        if (isLogoAlreadyGuessed(logosList[currentIndex].id, level)) {
            Toast.makeText(this, "To logo już zostało odgadnięte!", Toast.LENGTH_SHORT).show()
            return
        }
        if (hintPoints > 0) {
            hintPoints--
            sharedPref.edit().putInt("HINT_POINTS", hintPoints).apply()
            val usedHints = sharedPref.getInt("USED_HINTS", 0)
            sharedPref.edit().putInt("USED_HINTS", usedHints + 1).apply()
            updatePointsDisplay()

            val hintText = if (hintNumber == 1) logosList[currentIndex].p1 else logosList[currentIndex].p2
            val hintView = if (hintNumber == 1) hintP1TextView else hintP2TextView

            hintView.text = hintText
            hintView.visibility = View.VISIBLE

            hintView.postDelayed({
                hintView.visibility = View.GONE
            }, 10000)
        } else {
            Toast.makeText(this, "Brak dostępnych wskazówek!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupGame(correctAnswer: String) {
        answerContainer.removeAllViews()
        lettersContainer.removeAllViews()
        letterButtons.clear()
        answerSlots.clear()

        val dp = resources.displayMetrics.density
        val tileSize = (40 * dp).toInt()
        val marginSize = (4 * dp).toInt()

        for (i in correctAnswer.indices) {
            val textView = TextView(this).apply {
                text = "_"
                textSize = 24f
                setTextColor(Color.WHITE)
                background = ContextCompat.getDrawable(this@KatActivity, R.drawable.kafelek_zga)
                gravity = Gravity.CENTER
                layoutParams = FlexboxLayout.LayoutParams(tileSize, tileSize).apply {
                    setMargins(marginSize, marginSize, marginSize, marginSize)
                }
            }
            answerSlots.add(textView)
            answerContainer.addView(textView)
        }

        val extraLettersCount = minOf(6, 12 - correctAnswer.length)
        val shuffledLetters = (correctAnswer.toList() + generateRandomLetters(extraLettersCount)).shuffled()

        shuffledLetters.forEachIndexed { index, letter ->
            val button = Button(this).apply {
                text = letter.toString()
                setTextColor(Color.BLACK)
                background = ContextCompat.getDrawable(this@KatActivity, R.drawable.kafelek)
                layoutParams = FlexboxLayout.LayoutParams(tileSize, tileSize).apply {
                    setMargins(marginSize, marginSize, marginSize, marginSize)
                }
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.CENTER
                setOnClickListener {
                    musicService?.playClickSound()
                    onLetterClicked(this)
                }
            }
            letterButtons.add(button)
            lettersContainer.addView(button)
        }
    }

    private fun onLetterClicked(button: Button) {
        val letter = button.text[0]
        button.visibility = View.INVISIBLE

        for (textView in answerSlots) {
            if (textView.text.toString().isBlank() || textView.text.toString() == "_") {
                textView.text = letter.toString()
                break
            }
        }
        checkAnswer()
    }

    private fun saveTotalPoints(points: Int) {
        val totalPoints = sharedPref.getInt("TOTAL_POINTS", 0)
        val mode2Points = sharedPref.getInt("MODE2_POINTS", 0)

        sharedPref.edit()
            .putInt("TOTAL_POINTS", totalPoints + points)
            .putInt("MODE2_POINTS", mode2Points + points)
            .apply()

        val addedHints = points / 1
        if (addedHints > 0) {
            hintPoints += addedHints
            sharedPref.edit().putInt("HINT_POINTS", hintPoints).apply()
        }

        val level = intent.getIntExtra("CATEGORY", -1)
        if (level != -1) {
            val levelPointsKey = "POINTS_KAT$level"
            val levelPoints = sharedPref.getInt(levelPointsKey, 0) + points
            sharedPref.edit().putInt(levelPointsKey, levelPoints).apply()
        }

        updatePointsDisplay()
    }

    private fun markLogoAsGuessed(logoId: Int, level: Int) {
        val guessedLogos = sharedPref.getStringSet("GUESSED_LOGOS_KAT$level", mutableSetOf()) ?: mutableSetOf()
        guessedLogos.add(logoId.toString())
        sharedPref.edit().putStringSet("GUESSED_LOGOS_KAT$level", guessedLogos).apply()
    }

    private fun isLogoAlreadyGuessed(logoId: Int, level: Int): Boolean {
        val guessedLogos = sharedPref.getStringSet("GUESSED_LOGOS_KAT$level", mutableSetOf()) ?: mutableSetOf()
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
        val category = intent.getIntExtra("CATEGORY", 1)
        val summaryIntent = Intent(this, SummaryActivity::class.java)
        summaryIntent.putExtra("COMPLETED_CATEGORY", category)
        startActivity(summaryIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        correctSound.release()
        wrongSound.release()
    }
}

@Serializable
data class Kategoria(
    val id: Int,
    val name: String,
    val image: String,
    val points: Int,
    val p1: String,
    val p2: String
)