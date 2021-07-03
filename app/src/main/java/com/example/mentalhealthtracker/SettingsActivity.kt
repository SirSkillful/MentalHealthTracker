package com.example.mentalhealthtracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.*

class SettingsActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val dbHelper = DatabaseHelper(this)

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            super.onBackPressed()
        }

        val spinner = findViewById<Spinner>(R.id.setting_rating_spinner)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.d("SPINNER", "Ich glaub ich spinne")
                populateEntries()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                return
            }
        }

        val applyButton = findViewById<Button>(R.id.apply_button)
        applyButton.setOnClickListener{
            val text = findViewById<EditText>(R.id.text_input).text.toString()
            val rating = findViewById<EditText>(R.id.rating_input).text.toString().toInt()
            val color = findViewById<EditText>(R.id.color_input).text.toString()
            val success = dbHelper.updateSetting(text, rating, color)
            val lastPosition = updateSpinnerEntries()
            spinner.setSelection(lastPosition)
            if (success) {
                val text = "Your rating has been updated"
                val duration = Toast.LENGTH_LONG

                val toast = Toast.makeText(applicationContext, text, duration)
                toast.show()
            }
        }

        val deleteButton = findViewById<Button>(R.id.delete_button)
        deleteButton.setOnClickListener{
            dbHelper.resetHistory()

            val text = "All entries have been reset"
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(applicationContext, text, duration)
            toast.show()
        }
    }

    override fun onResume() {
        super.onResume()
        //Load the rating options from the database
        updateSpinnerEntries()
        //Fill the text edits
    }

    /**
     * Update the spinner entries
     *
     * @return The last position of the spinner before the update
     */
    fun updateSpinnerEntries(): Int{
        val dbHelper = DatabaseHelper(this)
        val results:List<String> = dbHelper.getSettings()
        val dataAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, results)
        val spinner: Spinner = findViewById<Spinner>(R.id.setting_rating_spinner)
        val lastPos = spinner.selectedItemPosition
        spinner.adapter = dataAdapter
        return lastPos
    }

    fun populateEntries(){
        val dbHelper = DatabaseHelper(this)
        val spinner: Spinner = findViewById<Spinner>(R.id.setting_rating_spinner)
        val rating = spinner.selectedItem.toString().split("- ").last().toInt()
        Log.d("SPINNER", "Current rating is $rating")
        val setting = dbHelper.getSetting(rating)
        //Fill all fields accordingly
        if (setting != null){
            val textInput = findViewById<EditText>(R.id.text_input)
            textInput.setText(setting.getTextString())
            val ratingInput = findViewById<EditText>(R.id.rating_input)
            ratingInput.setText(setting.getRatingInt().toString())
            val colorInput = findViewById<EditText>(R.id.color_input)
            colorInput.setText(setting.getColorString())
            Log.d("SPINNER", "The setting values are ${setting.getTextString()}, ${setting.getRatingInt()} and ${setting.getColorString()}")
        } else {
            Log.d("SPINNER", "Guess the database had a problem")
            return
        }
    }


}