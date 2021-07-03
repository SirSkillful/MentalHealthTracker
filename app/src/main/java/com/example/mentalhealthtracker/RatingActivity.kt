package com.example.mentalhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RatingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rating_activity)

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            super.onBackPressed()
        }


        val confirmButton = findViewById<Button>(R.id.rating_confirm_button)
        confirmButton.setOnClickListener {
            //TODO Upload information to the database
            //Go back to the main activity
            super.onBackPressed()
        }

        val abortButton = findViewById<Button>(R.id.rating_cancel_button)
        abortButton.setOnClickListener {
            //Go back to the main activity
            super.onBackPressed()
        }
    }

    //TODO Create a function to get the data from the database for a specific date

    //TODO Create a function to push data to the database
}