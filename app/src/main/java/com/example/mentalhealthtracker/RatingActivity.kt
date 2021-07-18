package com.example.mentalhealthtracker

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RatingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rating_activity) // Load rating view

        val dbHelper = DatabaseHelper(this) // DB communicator

        val backButton = findViewById<Button>(R.id.back_button) // Button to return to the previous activity
        backButton.setOnClickListener {
            super.onBackPressed()
        }

        val confirmButton = findViewById<Button>(R.id.rating_confirm_button) // Button to confirm a rating and return to previous activity
        confirmButton.setOnClickListener {
            // Get the information of a rating for the upload to the database
            val note = findViewById<EditText>(R.id.text_box).text.toString()
            val date = findViewById<TextView>(R.id.date_text).text
            // Get the rating from the rating option and convert it to an int
            val rating = findViewById<Spinner>(R.id.rating_rating_spinner).selectedItem.toString().split("- ").last().toInt()
            // Upload the rating to the database
            dbHelper.saveRating(date as String, rating, note)
            // Go back to the main activity
            respondToRating(rating)
            super.onBackPressed()
        }

        val abortButton = findViewById<Button>(R.id.rating_cancel_button) // Button to go back to the previous activity
        abortButton.setOnClickListener {
            //Go back to the main activity
            super.onBackPressed()
        }
    }

    /**
     * Overriden onResume() function that is called when the activity is switched to.
     * Parses the information from intent package, retrieves data from the database and populates
     * the entries of the rating (chosen rating and note)
     */
    override fun onResume() {
        super.onResume()
        //Set the date label
        val currentDate = intent.getStringExtra("date")
        val dateText = findViewById<TextView>(R.id.date_text)
        dateText.text = currentDate
        //Load the rating options from the database
        val dbHelper = DatabaseHelper(this)
        val results:List<String> = dbHelper.getSettings()
        // Populate the spinner with the entries from the database
        val spinner = findViewById<Spinner>(R.id.rating_rating_spinner) // Spinner that holds the ratings
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
        val dbHelper = DatabaseHelper(this) // DB communicator
        // Get the rating for the selected date
        val rating = dbHelper.getRating(date as String)
        // Populate the rating and note field only if a rating for the given day has been found
        if (rating != null){
            // Select the right spinner
            val spinner = findViewById<Spinner>(R.id.rating_rating_spinner)
            val adapter = spinner.adapter
            // Set the spinner value based on the rating integer
            for (i in 0 until adapter.count){
                val item = adapter.getItem(i).toString()
                // Set the spinner item if the rating option with the given rating int has been found
                if (item.split("- ").last() == rating.rating.toString()){
                    spinner.setSelection(i)
                    break
                }
            }
            val note = findViewById<EditText>(R.id.text_box)
            note.setText(rating.getNoteString())
        } else {
            return
        }
    }

    /**
     * Show a toast on screen, either for a long or short time
     * Just a little wrapper to clean up the cluster of creating a toast each time
     * @param text String containing the text shown on screen
     * @param duration Int from Toast.LENGTH_LONG or TOAST.LENGTH_SHORT, so 0 or 1 to set the duration of the toast
     */
    fun showToast(text: String, duration: Int){
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }

    /**
     * Post a toast as a response to a given rating
     * @param rating Int that holds the rating. The higher the value the better
     */
    fun respondToRating(rating: Int){
        if (rating <= 1){
            showToast("Sorry your day hasn't been great. Tomorrow will surely be better.", Toast.LENGTH_LONG)
        } else if (rating == 2) {
            showToast("We all have some bad days but at least this one is over. Let's look forward to greater days!", Toast.LENGTH_LONG)
        } else if (rating == 3) {
            showToast("Just a normal day, that's cool. If it weren't for them we could not tell the amazing ones apart!", Toast.LENGTH_LONG)
        } else if (rating == 4) {
            showToast("Another good day, you deserve it!", Toast.LENGTH_LONG)
        } else {
            showToast("Great to hear you had a wonderful day! Let me know all about it :)", Toast.LENGTH_LONG)
        }
    }
}