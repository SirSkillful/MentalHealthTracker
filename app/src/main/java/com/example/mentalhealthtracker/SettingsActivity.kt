package com.example.mentalhealthtracker

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.skydoves.colorpickerview.AlphaTileView
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class SettingsActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?){
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
            val rating = findViewById<EditText>(R.id.rating_number).text.toString().toInt()
            val color = findViewById<EditText>(R.id.color_input).text.toString()
            // Try setting the color of the button. If it's not possible the color is invalid and the DB will not be updated
            val validColor =             try{
                val colorPickerButton = findViewById<Button>(R.id.color_picker_button)
                colorPickerButton.setBackgroundColor(Color.parseColor(color))
            } catch (e: java.lang.IllegalArgumentException){
                showToast("Please enter a valid color before updating", Toast.LENGTH_SHORT)
                -1
            }

            val createBox = findViewById<CheckBox>(R.id.create_checkbox)
            if (validColor != -1){

                val success = dbHelper.updateSetting(text, rating, color)
                val lastPosition = updateSpinnerEntries()

                // Create a new setting
                if(createBox.isChecked){
                    val response = dbHelper.addSetting(text, rating, color)
                    if (response){
                        val lastPosition = updateSpinnerEntries()
                        spinner.setSelection(lastPosition)
                        showToast("Your rating has been created successfully", Toast.LENGTH_LONG)
                    } else {
                        showToast("No rating could be created, please check that the rating is a number and not a duplicate", Toast.LENGTH_LONG)
                    }
                } else {
                    // Update an existing rating
                    spinner.setSelection(lastPosition)
                    if (success) {
                        showToast("Your rating has been updated", Toast.LENGTH_LONG)
                    } else {
                        showToast("The rating could not be updated, please check if such a rating exists", Toast.LENGTH_LONG)
                    }
                }
            }

        }

        val deleteEntryButton = findViewById<Button>(R.id.delete_setting_button)
        deleteEntryButton.setOnClickListener {
            val rating = findViewById<EditText>(R.id.rating_number).text.toString().toInt()
            val response = dbHelper.deleteSetting(rating) // If a rating option is deleted all rated days are as well
            if(response){
                showToast("The rating has been deleted successfully", Toast.LENGTH_LONG)
                updateSpinnerEntries()
                populateEntries()
            } else {
                showToast("Your rating could not be deleted, please make sure the selection is valid", Toast.LENGTH_LONG)
            }
        }

        val deleteButton = findViewById<Button>(R.id.delete_button)
        deleteButton.setOnClickListener{
            dbHelper.resetHistory()
            showToast("All entries have been reset", Toast.LENGTH_LONG)
        }

        val pickerButton = findViewById<Button>(R.id.color_picker_button)
        pickerButton.setOnClickListener {
            val builder = ColorPickerDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Pick your color")
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton("Confirm",
                    ColorEnvelopeListener { envelope, fromUser -> setColor(envelope) })
                .setNegativeButton("Cancel"
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .attachAlphaSlideBar(true) // the default value is true.
                .attachBrightnessSlideBar(true) // the default value is true.
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.

            builder.colorPickerView.setFlagView(BubbleFlag(this))
            builder.colorPickerView.setBackgroundColor(Color.parseColor("#83D8CF"))
            builder.show()
        }

        val resetSettingsButton = findViewById<Button>(R.id.reset_settings_button)
        resetSettingsButton.setOnClickListener{
            val response = dbHelper.resetSettings()
            showToast("The settings have been reset", Toast.LENGTH_LONG)
            if (response){
                showToast("History entries have been affected as well", Toast.LENGTH_LONG)
            }
            updateSpinnerEntries()
            populateEntries()
        }

        //Make sure the color is valid so the app doesn't crash
        val colorTextEdit = findViewById<EditText>(R.id.color_input)
        val maxColorString:Int = 9
        colorTextEdit.doAfterTextChanged {
            if (colorTextEdit.text.length > maxColorString){
                val droppedChars = colorTextEdit.text.length - maxColorString
                Log.d("COLOR", "$droppedChars chars to be dropped at length ${colorTextEdit.text.length}")
                colorTextEdit.setText(colorTextEdit.text.dropLast(droppedChars))
            }
            if (colorTextEdit.text.length < 1){
                colorTextEdit.setText("#")
            }
            Log.d("COLOR", "This is the color code ${colorTextEdit.text}")
            try{
                val colorPickerButton = findViewById<Button>(R.id.color_picker_button)
                colorPickerButton.setBackgroundColor(Color.parseColor(colorTextEdit.text.toString()))
            } catch (e: java.lang.IllegalArgumentException){
                showToast("Please enter a valid color", Toast.LENGTH_SHORT)
            }
        }
    }

    private fun setColor(envelope: ColorEnvelope) {
        val colorText = findViewById<EditText>(R.id.color_input)
        colorText.setText("#${envelope.getHexCode()}")
        //Update color of the color button
        val colorPickerButton = findViewById<Button>(R.id.color_picker_button)
        colorPickerButton.setBackgroundColor(Color.parseColor("#${envelope.getHexCode()}"))
    }

    fun showToast(text: String, duration: Int){
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }

    override fun onResume() {
        super.onResume()
        //Load the rating options from the database
        updateSpinnerEntries()
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
            val ratingInput = findViewById<EditText>(R.id.rating_number)
            ratingInput.setText(setting.getRatingInt().toString())
            val colorInput = findViewById<EditText>(R.id.color_input)
            colorInput.setText(setting.getColorString())
            //Change the color of the color picker button
            val colorPickerButton = findViewById<Button>(R.id.color_picker_button)
            colorPickerButton.setBackgroundColor(Color.parseColor(setting.getColorString()))
            Log.d("SPINNER", "The setting values are ${setting.getTextString()}, ${setting.getRatingInt()} and ${setting.getColorString()}")
        } else {
            Log.d("SPINNER", "Guess the database had a problem")
            return
        }
    }
}