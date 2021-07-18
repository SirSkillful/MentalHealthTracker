package com.example.mentalhealthtracker

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class SettingsActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val dbHelper = DatabaseHelper(this) // DB communicator

        val backButton = findViewById<Button>(R.id.back_button) // Button to return to the previous activity
        backButton.setOnClickListener {
            super.onBackPressed()
        }

        // Overwrite the spinner listener to populate the text boxes when the selection changes
        val spinner = findViewById<Spinner>(R.id.setting_rating_spinner) // Spinner that holds all the rating options
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                populateEntries()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                return
            }
        }

        // Update or create a setting when the apply button is pressed
        val applyButton = findViewById<Button>(R.id.apply_button)
        applyButton.setOnClickListener{
            val text = findViewById<EditText>(R.id.text_input).text.toString() // Description of the rating option

            val rating = try {
                findViewById<EditText>(R.id.rating_number).text.toString().toInt()
            } catch (e: java.lang.NumberFormatException){ // Catch exception if the rating is not an integer and notify the user
                showToast("Please enter a valid rating", Toast.LENGTH_SHORT)
                -1 // Flag that the rating is invalid so that the DB will not be updated later
            }

            val color = findViewById<EditText>(R.id.color_input).text.toString()
            // Try setting the color of the button. If it's not possible the color is invalid and the DB will not be updated
            val validColor = try{
                try {
                    val colorPickerButton = findViewById<Button>(R.id.color_picker_button)
                    colorPickerButton.setBackgroundColor(Color.parseColor(color))
                } catch (e: java.lang.StringIndexOutOfBoundsException){ // Color does not exist
                    showToast("Please enter a valid color before updating", Toast.LENGTH_SHORT)
                    -1 // Flag that the color is invalid so that the DB will not be updated later
                }
            } catch (e: java.lang.IllegalArgumentException){ // Color string too short or  empty
                showToast("Please enter a valid color before updating", Toast.LENGTH_SHORT)
                -1 // Flag that the color is invalid so that the DB will not be updated later
            }

            val createBox = findViewById<CheckBox>(R.id.create_checkbox) // Checkbox that signals if the user wants to create a new rating option
            if (validColor != -1 && rating != -1){ // Only create or update the rating option if the color and rating are valid entries

                var lastPosition = -1 // Position of the spinner before uploading the new rating option

                // Create a new setting
                if(createBox.isChecked){
                    val response = dbHelper.addSetting(text, rating, color) // Add a new setting to the DB
                    if (response){ // Update the spinners entries and keep the last selected spinner entry on screen if the creation was successful
                        showToast("Your rating has been created successfully", Toast.LENGTH_LONG)
                        // Set the spinner to the
                        lastPosition = updateSpinnerEntries() // Update spinner entries and get the position of the last selection
                        if (lastPosition != -1){ // Position is not invalid
                            spinner.setSelection(lastPosition)
                        }
                    } else { // Creation of the rating option was not successful
                        showToast("No rating could be created, please check that the rating is not a duplicate and the text isn't empty", Toast.LENGTH_LONG)
                    }
                // Update an existing setting
                } else {
                    val success = dbHelper.updateSetting(text, rating, color) // Update the rating option in the DB
                    // Update an existing rating
                    if (success) {
                        showToast("Your rating has been updated", Toast.LENGTH_LONG)
                        lastPosition = updateSpinnerEntries() // Update the spinner entries with the newest rating and get position of the last selection which was edited
                        if (lastPosition != -1){ // Position is not invalid
                            spinner.setSelection(lastPosition)
                        }
                    } else { // Update was not successful
                        showToast("The rating could not be updated, please check if such a rating exists", Toast.LENGTH_LONG)
                    }
                }
            }

        }

        val deleteEntryButton = findViewById<Button>(R.id.delete_setting_button) // Button to delete a selected rating option
        deleteEntryButton.setOnClickListener {
            val rating = findViewById<EditText>(R.id.rating_number).text.toString().toInt() // Identifying value for a rating option
            val response = dbHelper.deleteSetting(rating) // If a rating option is deleted all rated days are as well
            if(response){ // If the deletion was succesful notify the user
                showToast("The rating has been deleted successfully", Toast.LENGTH_LONG)
                updateSpinnerEntries() // Get the newest spinner entries
                if (spinner.adapter.count >= 1) { // Fill the text boxes only if a rating option is given
                    populateEntries()
                }
            } else {
                showToast("Your rating could not be deleted, please make sure the selection is valid", Toast.LENGTH_LONG)
            }
        }

        val deleteHistoryButton = findViewById<Button>(R.id.delete_button) // Button to delete all rating entries
        deleteHistoryButton.setOnClickListener{
            dbHelper.resetHistory() // Delete all ratings for all days
            showToast("All entries have been reset", Toast.LENGTH_LONG)
        }

        val pickerButton = findViewById<Button>(R.id.color_picker_button) // Button for picking a color
        pickerButton.setOnClickListener {
            // Create a color picker dialog
            val builder = ColorPickerDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Pick your color") // Title of the dialog
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton("Confirm", // Confirm button definition
                    ColorEnvelopeListener { envelope, fromUser -> setColor(envelope) }) // Callback the return value of the dialog is passed to after confirming a color
                .setNegativeButton("Cancel" // Cancel button definition
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .setBottomSpace(12) // Space between the sliders and the control buttons

            builder.colorPickerView.flagView = BubbleFlag(this) // Attach a flag to the color selection
            builder.colorPickerView.setBackgroundColor(Color.parseColor("#83D8CF")) // Set the background color of the dialog to the standard background color of the app
            builder.show()
        }

        val resetSettingsButton = findViewById<Button>(R.id.reset_settings_button) // Button to reset the settings back to default
        resetSettingsButton.setOnClickListener{
            val response = dbHelper.resetSettings() // Reset the settings to default in the DB
            showToast("The settings have been reset", Toast.LENGTH_LONG)
            if (response){ // Notify the user that some ratings have been deleted because the rating options weren't containing default ratings values (1-5)
                showToast("History entries have been affected as well", Toast.LENGTH_LONG)
            }
            updateSpinnerEntries()
            populateEntries()
        }

        //Make sure the color is valid so the app doesn't crash
        val colorTextEdit = findViewById<EditText>(R.id.color_input)
        val maxColorString:Int = 9 // Maximum length of a valid hexcode color
        colorTextEdit.doAfterTextChanged {
            if (colorTextEdit.text.length > maxColorString){ // Cut the color string if it is too big
                val droppedChars = colorTextEdit.text.length - maxColorString
                colorTextEdit.setText(colorTextEdit.text.dropLast(droppedChars))
            }
            if (colorTextEdit.text.length < 1){ // The color always has to start with a hashtag
                colorTextEdit.setText("#")
            }
            // Try setting the color as given in the text field
            try{
                val colorPickerButton = findViewById<Button>(R.id.color_picker_button)
                colorPickerButton.setBackgroundColor(Color.parseColor(colorTextEdit.text.toString()))
            } catch (e: java.lang.IllegalArgumentException){ // If the color doesn't exist notify the user
                showToast("Please enter a valid color", Toast.LENGTH_SHORT)
            }
        }
    }

    /**
     * Callback function for parsing the return value of the color picker dialog
     * @param envelope ColorEnvelope object containing the return value of the color picker dialog as stated in the documentation by Jaewong Eum in the ColorPickerView on Github
     */
    private fun setColor(envelope: ColorEnvelope) {
        val colorText = findViewById<EditText>(R.id.color_input)
        colorText.setText("#${envelope.hexCode}")
        // Update color of the color button
        val colorPickerButton = findViewById<Button>(R.id.color_picker_button)
        colorPickerButton.setBackgroundColor(Color.parseColor("#${envelope.hexCode}"))
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
     * Overwritten onResume() function called when the activity is resumed
     * Gets all the rating options from the datapbase and updates the spinner
     */
    override fun onResume() {
        super.onResume()
        //Load the rating options from the database
        updateSpinnerEntries()
    }

    /**
     * Update the spinner entries with all entries in the database
     * @return The last position of the spinner before the update
     */
    fun updateSpinnerEntries(): Int{
        val dbHelper = DatabaseHelper(this) // DB communicator
        val results:List<String> = dbHelper.getSettings() // List of all rating options in the DB
        val dataAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, results) // Adapter for the spinner
        val spinner: Spinner = findViewById<Spinner>(R.id.setting_rating_spinner) // Spinner holding all rating option
        val lastPos = spinner.selectedItemPosition
        spinner.adapter = dataAdapter
        return lastPos
    }

    /**
     * Parse the selected spinner selection to populate all the text boxes
     */
    fun populateEntries(){
        val dbHelper = DatabaseHelper(this) // DB communicator
        val spinner: Spinner = findViewById<Spinner>(R.id.setting_rating_spinner) // Spinner holding the rating options
        val rating = spinner.selectedItem.toString().split("- ").last().toInt() // The rating always comes after the "- " section. Structure of a rating "Description - RatingValue"
        val setting = dbHelper.getSetting(rating) // Get all other information for a rating option not stated in the spinner e.g. the color
        // Fill all fields accordingly if a rating option exists for a given rating
        if (setting != null){
            val textInput = findViewById<EditText>(R.id.text_input)
            textInput.setText(setting.getTextString())
            val ratingInput = findViewById<EditText>(R.id.rating_number)
            ratingInput.setText(setting.getRatingInt().toString())
            val colorInput = findViewById<EditText>(R.id.color_input)
            colorInput.setText(setting.getColorString())
            // Change the color of the color picker button
            val colorPickerButton = findViewById<Button>(R.id.color_picker_button)
            colorPickerButton.setBackgroundColor(Color.parseColor(setting.getColorString()))
        } else { // Database could not retrieve an entry for a given rating
            return
        }
    }
}