package com.example.mentalhealthtracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import sun.bob.mcalendarview.MCalendarView
import sun.bob.mcalendarview.MarkStyle
import sun.bob.mcalendarview.listeners.OnDateClickListener
import sun.bob.mcalendarview.listeners.OnMonthChangeListener
import sun.bob.mcalendarview.vo.DateData
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*


class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_activity)

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            super.onBackPressed()
        }


        val calendarView =  findViewById<MCalendarView>(R.id.calendar_view)
        calendarView.setOnDateClickListener(object : OnDateClickListener() {
            /**
             * Overwritten onDateClick function of the calendar view
             * Get the selected date and start the rating activity with the data passed to it
             */
            override fun onDateClick(view: View?, date: DateData) {
                val calendar = Calendar.getInstance()
                calendar.set(date.year, date.month-1, date.day) // Month -1 because apparently that's just how it works?
                val sdf = SimpleDateFormat("dd.MM.yyyy") // Standard german date format
                val currentDate = sdf.format(calendar.timeInMillis) // Get the selected date in the specified format
                val intent = Intent(applicationContext, RatingActivity::class.java) // Rating activity intent
                intent.putExtra("date", currentDate) // Add date to pass to the rating activity for it to load the rating for it
                startActivity(intent)
            }
        })

        calendarView.setOnMonthChangeListener(object : OnMonthChangeListener() { // Change the text view showing the month's name and the year at the top of the calendar view
            override fun onMonthChange(year: Int, month: Int) {
                val dateText = findViewById<TextView>(R.id.calendar_month_text)
                val monthString = DateFormatSymbols().getMonths()[month-1] // Month -1 because apparently that's just how it works here as well?
                dateText.setText("$monthString - $year") // Set the text view text to show the month's name and year in the shown format
            }
        })
    }

    /**
     * Overwritten onResume function called when activity is resumed
     * Reloads all the markings when the history activity is opened
     */
    override fun onResume(){
        super.onResume()
        markDates()
        val sdf = SimpleDateFormat("MMMM - yyyy") // Month name and year
        val currentDate = sdf.format(Date())
        val dateText = findViewById<TextView>(R.id.calendar_month_text)
        dateText.setText(currentDate) // Set the date text view to show the month's name and year in the format specified previously
    }

    /**
     * Mark all dates that have a rating in the database in the calendar in color
     */
    fun markDates(){
        val dbHelper = DatabaseHelper(this) // DB Communicator
        val dates = dbHelper.getAllRatings() // Get all ratings in the database
        val calendarView =  findViewById<MCalendarView>(R.id.calendar_view)
        val prevMarks =  calendarView.markedDates.all.toMutableList() // All marked dates in the calendar view before the update

        val iterator = prevMarks.iterator() // Use an iterator because the original list must not be altered while it is traversed
        while (iterator.hasNext()){ // Remove all previous markings
            // Markings are removed and not updated because they cannot be overwritten with the new color
            val item = iterator.next()
            calendarView.unMarkDate(item)
        }

        for (date in dates){ // Mark each day for which a rating exists with the color stated in the database for the given entry
            calendarView.markDate(
                DateData(date.year, date.month, date.day).setMarkStyle(MarkStyle(MarkStyle.BACKGROUND, Color.parseColor(date.color)))
            )
        }
    }
}