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
        setContentView(R.layout.activity_main)

        val dbHelper = DatabaseHelper(this)

        val rateButton = findViewById<Button>(R.id.rate_button)
        rateButton.setOnClickListener {
            val intent = Intent(this, RatingActivity::class.java)
            val sdf = SimpleDateFormat("dd.MM.yyyy")
            val currentDate = sdf.format(Date())
            intent.putExtra("date", currentDate)
            startActivity(intent)
        }

        val historyButton = findViewById<Button>(R.id.history_button)
        historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        val settingsButton = findViewById<Button>(R.id.settings_button)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val exitButton = findViewById<Button>(R.id.exit_button)
        exitButton.setOnClickListener {
            moveTaskToBack(true)
            exitProcess(-1)
        }
    }
}