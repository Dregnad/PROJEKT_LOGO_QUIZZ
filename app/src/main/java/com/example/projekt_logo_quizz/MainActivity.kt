package com.example.projekt_logo_quizz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import io.github.jan.supabase.createSupabaseClient as createSupabaseClient1
import android.util.Log
import android.widget.Button


val supabase = createSupabaseClient1(
    supabaseUrl = "https://gtsbtjmwxllwgtioncvm.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd0c2J0am13eGxsd2d0aW9uY3ZtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDE5NzkxMjIsImV4cCI6MjA1NzU1NTEyMn0.5-C1i96tsA3QN2czxm9yhYj6iVoD4ky3AdC7GOgmHz8"
) {
    install(Postgrest)
}

@Serializable
data class Instrument(
    val id: Int? = null,
    val name: String
)


fun addInstrument(name: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d("Supabase", "Dodawanie instrumentu: $name")

            val response = supabase.from("instruments").insert(Instrument(name = name))

            Log.d("Supabase", "Dodano instrument: $response")
        } catch (e: Exception) {
            Log.e("Supabase", "Błąd dodawania instrumentu: ${e.message}")
        }
    }
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAddInstrument = findViewById<Button>(R.id.btnAddInstrument)
        btnAddInstrument.setOnClickListener {
            addInstrument("Guitar")
        }
    }
}