package com.example.mentalhealthtracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_activity)

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            super.onBackPressed()
        }
    }
}