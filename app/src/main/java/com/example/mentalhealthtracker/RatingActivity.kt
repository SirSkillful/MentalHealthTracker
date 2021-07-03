package com.example.mentalhealthtracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class RatingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rating_activity)

        val dbHelper = DatabaseHelper(this)

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            super.onBackPressed()
        }


        val confirmButton = findViewById<Button>(R.id.rating_confirm_button)
        confirmButton.setOnClickListener {
            //Upload information entered to the database
            val note = findViewById<EditText>(R.id.text_box).getText().toString()
            val date = findViewById<TextView>(R.id.date_text).text
            val rating = findViewById<Spinner>(R.id.rating_rating_spinner).selectedItem.toString().split("- ").last().toInt()
            dbHelper.saveRating(date as String, rating, note)
            //Go back to the main activity
            super.onBackPressed()
        }

        val abortButton = findViewById<Button>(R.id.rating_cancel_button)
        abortButton.setOnClickListener {
            //Go back to the main activity
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        //Set the date label
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val currentDate = sdf.format(Date())
        val dateText = findViewById<TextView>(R.id.date_text)
        dateText.text = currentDate
        //Load the rating options from the database
        val dbHelper = DatabaseHelper(this)
        val results:List<String> = dbHelper.getSettings()
        val spinner = findViewById<Spinner>(R.id.rating_rating_spinner)
        val dataAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, results)
        spinner.adapter = dataAdapter
        //Populate the entries
        populateEntries()
    }

    /**
     * Populate the textedit and rating based on the selected date
     */
    fun populateEntries(){
        val date = findViewById<TextView>(R.id.date_text).text
        val dbHelper = DatabaseHelper(this)
        val rating = dbHelper.getRating(date as String)
        if (rating != null){
            // Select the right spinner
            val spinner = findViewById<Spinner>(R.id.rating_rating_spinner)
            spinner.setSelection(rating.getRatingInt()-1)
            val note = findViewById<EditText>(R.id.text_box)
            note.setText(rating.getNoteString())
        } else {
            return
        }
    }

    //TODO Create a function to get the data from the database for a specific date

    //TODO Create a function to push data to the database
}