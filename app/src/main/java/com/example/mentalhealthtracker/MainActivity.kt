package com.example.mentalhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Load main view

        val rateButton = findViewById<Button>(R.id.rate_button) // Button to switch to rating view
        rateButton.setOnClickListener {
            // Start the rating activity
            val intent = Intent(this, RatingActivity::class.java)
            val sdf = SimpleDateFormat("dd.MM.yyyy") // German date format
            val currentDate = sdf.format(Date())
            intent.putExtra("date", currentDate) // Pass the date to the rating activity for it to load the given day
            startActivity(intent)
        }

        val historyButton = findViewById<Button>(R.id.history_button) // Button to switch to history view
        historyButton.setOnClickListener {
            // Start history activity
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        val settingsButton = findViewById<Button>(R.id.settings_button) // Button to switch to the settings view
        settingsButton.setOnClickListener {
            // Start the settings activity
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val exitButton = findViewById<Button>(R.id.exit_button) // Button to exit the application
        exitButton.setOnClickListener {
            moveTaskToBack(true)
            exitProcess(-1)
        }
    }
}