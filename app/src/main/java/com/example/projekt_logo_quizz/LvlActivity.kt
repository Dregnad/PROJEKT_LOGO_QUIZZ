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
import android.widget.LinearLayout
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

class LvlActivity : AppCompatActivity() {

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
    private lateinit var resetButton: Button
    private lateinit var hintP1TextView: TextView
    private lateinit var hintP2TextView: TextView
    private lateinit var letterButtons: MutableList<Button>



    private lateinit var answerSlots: MutableList<TextView>
    private lateinit var pointsTextView: TextView
    private var correctAnswer: String = ""
    private var currentIndex: Int = 0
    private var logosList: List<Logo> = listOf()
    private var showingHints = false
    private var hintPoints = 0
    private lateinit var hintRevealedSlots: MutableList<Boolean>
    private lateinit var sharedPref: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lvl_quiz)

        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }



        logoImageView = findViewById(R.id.logoImageView)
        answerContainer = findViewById(R.id.answerContainer)
        lettersContainer = findViewById(R.id.lettersContainer)
        pointsTextView = findViewById(R.id.pointsTextView)
        hintP1TextView = findViewById(R.id.hintP1)
        hintP2TextView = findViewById(R.id.hintP2)



        findViewById<ImageButton>(R.id.btnNext).setOnClickListener {
            musicService?.playClickSound()
            nextLogo()
        }
        findViewById<ImageButton>(R.id.btnPrev).setOnClickListener {
            musicService?.playClickSound()
            prevLogo() }
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            musicService?.playClickSound()
            val intent = Intent(this, CategoryActivity::class.java)
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
            val level = intent.getIntExtra("LEVEL", -1)
            if (isLogoAlreadyGuessed(logosList[currentIndex].id, level)) {
                Toast.makeText(this, "To logo już zostało odgadnięte!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hintPoints >= 2) {
                revealRandomLetter()
                hintPoints -= 2
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
            val level = intent.getIntExtra("LEVEL", -1)
            if (isLogoAlreadyGuessed(logosList[currentIndex].id, level)) {
                Toast.makeText(this, "To logo już zostało odgadnięte!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hintPoints > 0) {
                val correctLetters = correctAnswer.toSet()
                val wrongButtons = letterButtons.filter {
                    it.visibility == View.VISIBLE && it.text[0] !in correctLetters
                }
                if (wrongButtons.isNotEmpty()) {
                    removeOneWrongLetter()
                    hintPoints--
                    sharedPref.edit().putInt("HINT_POINTS", hintPoints).apply()
                    val usedHints = sharedPref.getInt("USED_HINTS", 0)
                    sharedPref.edit().putInt("USED_HINTS", usedHints + 1).apply()
                    updatePointsDisplay()
                } else {
                    Toast.makeText(this, "Nie ma już błędnych liter do usunięcia!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Brak dostępnych wskazówek!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageButton>(R.id.podpowiedz3).setOnClickListener {
            musicService?.playClickSound()
            val level = intent.getIntExtra("LEVEL", -1)
            if (isLogoAlreadyGuessed(logosList[currentIndex].id, level)) {
                Toast.makeText(this, "To logo już zostało odgadnięte!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hintPoints >= 10) {
                hintPoints -= 10
                sharedPref.edit().putInt("HINT_POINTS", hintPoints).apply()
                updatePointsDisplay()
                skipCurrentLogo()
                val usedHints = sharedPref.getInt("USED_HINTS", 0)
                sharedPref.edit().putInt("USED_HINTS", usedHints + 1).apply() // Zwiększ licznik
                updatePointsDisplay()
            } else {
                Toast.makeText(this, "Brak dostępnych wskazówek!", Toast.LENGTH_SHORT).show()
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
            val level = intent.getIntExtra("LEVEL", -1)
            if (isLogoAlreadyGuessed(logosList[currentIndex].id, level)) {
                Toast.makeText(this, "To logo już zostało odgadnięte!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hintPoints >= 4) {
                val firstIndex = 0
                val lastIndex = correctAnswer.length - 1
                if (answerSlots[firstIndex].text != "_" && answerSlots[lastIndex].text != "_") {
                    Toast.makeText(this, "Pierwsza i ostatnia litera są już ujawnione!", Toast.LENGTH_SHORT).show()
                } else {
                    revealFirstAndLastLetter()
                    hintPoints -= 4
                    sharedPref.edit().putInt("HINT_POINTS", hintPoints).apply()
                    val usedHints = sharedPref.getInt("USED_HINTS", 0)
                    sharedPref.edit().putInt("USED_HINTS", usedHints + 1).apply()
                    updatePointsDisplay()
                }
            } else {
                Toast.makeText(this, "Brak dostępnych wskazówek!", Toast.LENGTH_SHORT).show()
            }
        }

        letterButtons = mutableListOf()
        answerSlots = mutableListOf()
        sharedPref = getSharedPreferences("GamePrefs", MODE_PRIVATE)
        hintPoints = sharedPref.getInt("HINT_POINTS", 0)

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

        loadLogosForLevel(level)
        updatePointsDisplay()
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


    private fun setHintButtonsEnabled(enabled: Boolean) {
        findViewById<ImageButton>(R.id.podpowiedz1).isEnabled = enabled
        findViewById<ImageButton>(R.id.podpowiedz2).isEnabled = enabled
        findViewById<ImageButton>(R.id.podpowiedz3).isEnabled = enabled
        findViewById<ImageButton>(R.id.podpowiedz4).isEnabled = enabled
        findViewById<ImageButton>(R.id.podpowiedz5).isEnabled = enabled
        findViewById<ImageButton>(R.id.podpowiedz6).isEnabled = enabled
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }



    }

    private fun generateRandomLetters(count: Int): List<Char> {
        val alphabet = ('A'..'Z').toList()
        return List(count) { alphabet.random() }
    }

    private fun findFirstUnsolvedLogo(): Int {
        val level = intent.getIntExtra("LEVEL", -1)
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
            markLevelAsCompleted(intent.getIntExtra("LEVEL", 1))
            goToSummary()
            return
        }

        val logo = logosList[currentIndex]
        correctAnswer = logo.name.uppercase()
        val level = intent.getIntExtra("LEVEL", -1)

        Picasso.get().load(logo.image).into(logoImageView)

        // Wstępne ładowanie następnych logo
        if (currentIndex + 1 < logosList.size) {
            Picasso.get().load(logosList[currentIndex + 1].image).fetch()
        }
        if (currentIndex + 2 < logosList.size) {
            Picasso.get().load(logosList[currentIndex + 2].image).fetch()
        }

        if (isLogoAlreadyGuessed(logo.id, level)) {
            setupGame(correctAnswer)
            disableInput()
            setHintButtonsEnabled(false)
        } else {
            setupGame(correctAnswer)
            setHintButtonsEnabled(true)
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
            slot.isClickable = false // Wyłącz klikalność
        }
    }

    private fun enableInput() {
        letterButtons.forEach { it.isEnabled = true }
        answerSlots.forEach { it.text = "_" }
    }

    private fun checkIfLevelCompleted(): Boolean {
        val level = intent.getIntExtra("LEVEL", -1)
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

        // Osiągnięcia za odgadnięcie logotypów
        if (guessedLogos >= 5 && !sharedPref.getBoolean("ACHIEVEMENT_5_LOGOS", false)) {
            sharedPref.edit().putBoolean("ACHIEVEMENT_5_LOGOS", true).apply()
            Toast.makeText(this, "Osiągnięcie odblokowane: Początek przygody!", Toast.LENGTH_LONG).show()
        }
        if (guessedLogos >= 40 && !sharedPref.getBoolean("ACHIEVEMENT_40_LOGOS", false)) {
            sharedPref.edit().putBoolean("ACHIEVEMENT_40_LOGOS", true).apply()
            Toast.makeText(this, "Osiągnięcie odblokowane: Entuzjasta logotypów!", Toast.LENGTH_LONG).show()
        }
        if (guessedLogos >= 80 && !sharedPref.getBoolean("ACHIEVEMENT_80_LOGOS", false)) {
            sharedPref.edit().putBoolean("ACHIEVEMENT_80_LOGOS", true).apply()
            Toast.makeText(this, "Osiągnięcie odblokowane: Mistrz logotypów!", Toast.LENGTH_LONG).show()
        }
        if (guessedLogos >= 200 && !sharedPref.getBoolean("ACHIEVEMENT_200_LOGOS", false)) {
            sharedPref.edit().putBoolean("ACHIEVEMENT_200_LOGOS", true).apply()
            Toast.makeText(this, "Osiągnięcie odblokowane: Ekspert!", Toast.LENGTH_LONG).show()
        }

        // Osiągnięcia za ukończenie poziomów
        if (completedLevels.contains("1") && !sharedPref.getBoolean("ACHIEVEMENT_LVL1", false)) {
            sharedPref.edit().putBoolean("ACHIEVEMENT_LVL1", true).apply()
            Toast.makeText(this, "Osiągnięcie odblokowane: Ekspert pierwszego poziomu!", Toast.LENGTH_LONG).show()
        }
        if (completedLevels.contains("5") && !sharedPref.getBoolean("ACHIEVEMENT_LVL5", false)) {
            sharedPref.edit().putBoolean("ACHIEVEMENT_LVL5", true).apply()
            Toast.makeText(this, "Osiągnięcie odblokowane: Połowa drogi!", Toast.LENGTH_LONG).show()
        }
        if (completedLevels.contains("7") && !sharedPref.getBoolean("ACHIEVEMENT_LVL7", false)) {
            sharedPref.edit().putBoolean("ACHIEVEMENT_LVL7", true).apply()
            Toast.makeText(this, "Osiągnięcie odblokowane: Ekspert poziomów!", Toast.LENGTH_LONG).show()
        }
        if (completedLevels.contains("10") && !sharedPref.getBoolean("ACHIEVEMENT_LVL10", false)) {
            sharedPref.edit().putBoolean("ACHIEVEMENT_LVL10", true).apply()
            Toast.makeText(this, "Osiągnięcie odblokowane: Niepowstrzymany!", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAnswer() {
        val userAnswer = answerSlots.joinToString("") { it.text.toString() }

        if (!userAnswer.contains("_")) {

            letterButtons.forEach { it.isEnabled = false }

            if (userAnswer.equals(correctAnswer, ignoreCase = true)) {
                Toast.makeText(this, "Brawo! Dobra odpowiedź!", Toast.LENGTH_SHORT).show()
                musicService?.playDobrySound()

                val level = intent.getIntExtra("LEVEL", -1)
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
                musicService?.playZlySound()

                resetGame()
            }
        }
    }

    private fun resetNonHintSlots() {
        val lettersToRestore = mutableListOf<String>()
        for (i in answerSlots.indices) {
            if (!hintRevealedSlots[i] && answerSlots[i].text != "_") {
                lettersToRestore.add(answerSlots[i].text.toString())
                answerSlots[i].text = "_"
            }
        }
        for (letter in lettersToRestore) {
            val button = letterButtons.find { it.text == letter && it.visibility == View.INVISIBLE }
            button?.visibility = View.VISIBLE
        }
    }

    private fun incrementGuessedLogos() {
        val guessedLogos = sharedPref.getInt("TOTAL_GUESSED_LOGOS", 0) + 1
        sharedPref.edit().putInt("TOTAL_GUESSED_LOGOS", guessedLogos).apply()
        checkAchievements()
    }

    private fun resetGame() {
        setupGame(correctAnswer)
        letterButtons.forEach { it.isEnabled = true }
    }
    private fun updatePointsDisplay() {
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
            hintRevealedSlots[index] = true // Oznacz jako ujawnione przez podpowiedź

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
            hintRevealedSlots[firstIndex] = true // Oznacz jako ujawnione
            val firstButton = letterButtons.find { it.text == firstChar && it.visibility == View.VISIBLE }
            firstButton?.visibility = View.INVISIBLE
        }

        if (answerSlots[lastIndex].text == "_" || answerSlots[lastIndex].text.isBlank()) {
            val lastChar = correctAnswer[lastIndex].toString()
            answerSlots[lastIndex].text = lastChar
            hintRevealedSlots[lastIndex] = true // Oznacz jako ujawnione
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
        val level = intent.getIntExtra("LEVEL", -1)
        if (level == -1) return

        val logo = logosList[currentIndex]
        if (!isLogoAlreadyGuessed(logo.id, level)) {
            saveTotalPoints(logo.points)
            incrementGuessedLogos()
            markLogoAsGuessed(logo.id, level)
            musicService?.playDobrySound()
            Thread.sleep(1000) // 500 ms opóźnienia, aby dźwięk się odtworzył
        }

        currentIndex = findFirstUnsolvedLogo()
        loadQuestion()
    }

    private fun setupGame(correctAnswer: String) {
        answerContainer.removeAllViews()
        lettersContainer.removeAllViews()
        letterButtons.clear()
        answerSlots.clear()

        val dp = resources.displayMetrics.density
        val tileSize = (40 * dp).toInt()
        val marginSize = (4 * dp).toInt()

        // Tworzenie slotów odpowiedzi z rozróżnieniem spacji i liter
        for (i in correctAnswer.indices) {
            val textView = TextView(this).apply {
                text = if (correctAnswer[i] == ' ') " " else "_"  // Spacja dla spacji, "_" dla liter
                textSize = 24f
                setTextColor(Color.WHITE)
                background = ContextCompat.getDrawable(this@LvlActivity, R.drawable.kafelek_zga)
                gravity = Gravity.CENTER
                layoutParams = FlexboxLayout.LayoutParams(tileSize, tileSize).apply {
                    setMargins(marginSize, marginSize, marginSize, marginSize)
                }
            }
            answerSlots.add(textView)
            answerContainer.addView(textView)
        }

        // Generowanie przycisków liter bez spacji
        val lettersInAnswer = correctAnswer.filter { it != ' ' }.toList()  // Tylko litery
        val extraLettersCount = maxOf(0, minOf(6, 20 - correctAnswer.length))
        val shuffledLetters = (lettersInAnswer + generateRandomLetters(extraLettersCount)).shuffled()

        shuffledLetters.forEachIndexed { index, letter ->
            val button = Button(this).apply {
                text = letter.toString()
                setTextColor(Color.BLACK)
                background = ContextCompat.getDrawable(this@LvlActivity, R.drawable.kafelek)
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
        val hasEmptySlot = answerSlots.any { it.text.toString() == "_" }
        if (!hasEmptySlot) {
            Log.d("LvlActivity", "Brak pustych slotów, ignoruję kliknięcie litery")
            return
        }

        val letter = button.text[0]
        button.visibility = View.INVISIBLE

        for (textView in answerSlots) {
            if (textView.text.toString() == "_") {
                textView.text = letter.toString()
                break
            }
        }
        checkAnswer()
    }

    private fun saveTotalPoints(points: Int) {
        val totalPoints = sharedPref.getInt("TOTAL_POINTS", 0)
        val mode1Points = sharedPref.getInt("MODE1_POINTS", 0)

        sharedPref.edit()
            .putInt("TOTAL_POINTS", totalPoints + points)
            .putInt("MODE1_POINTS", mode1Points + points)
            .apply()

        val addedHints = points / 1
        if (addedHints > 0) {
            hintPoints += addedHints
            sharedPref.edit().putInt("HINT_POINTS", hintPoints).apply()
        }

        val level = intent.getIntExtra("LEVEL", -1)
        if (level != -1) {
            val levelPointsKey = "POINTS_LVL$level"
            val levelPoints = sharedPref.getInt(levelPointsKey, 0) + points
            sharedPref.edit().putInt(levelPointsKey, levelPoints).apply()
        }

        updatePointsDisplay()
    }

    private fun showHint(hintNumber: Int) {
        val level = intent.getIntExtra("LEVEL", -1)
        if (isLogoAlreadyGuessed(logosList[currentIndex].id, level)) {
            Toast.makeText(this, "To logo już zostało odgadnięte!", Toast.LENGTH_SHORT).show()
            return
        }
        if (hintPoints >= 2) {
            hintPoints -= 2
            sharedPref.edit().putInt("HINT_POINTS", hintPoints).apply()
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
        val level = intent.getIntExtra("LEVEL", 1)
        val summaryIntent = Intent(this, SummaryActivity::class.java)
        summaryIntent.putExtra("COMPLETED_LEVEL", level)
        startActivity(summaryIntent)
        finish()
    }
}

@Serializable
data class Logo(
    val id: Int,
    val name: String,
    val image: String,
    val points: Int,
    val p1: String,
    val p2: String
)