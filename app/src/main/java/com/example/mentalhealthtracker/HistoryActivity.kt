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
        /**
         * Get the chosen date and pass it to the rating activity
         */

        calendarView.setOnDateClickListener(object : OnDateClickListener() {
            override fun onDateClick(view: View?, date: DateData) {
                val calendar = Calendar.getInstance()
                calendar.set(date.year, date.month-1, date.day)
                val sdf = SimpleDateFormat("dd.MM.yyyy")
                val currentDate = sdf.format(calendar.timeInMillis)
                Log.d("CALENDAR", "The following date was selected: $currentDate")
                val intent = Intent(applicationContext, RatingActivity::class.java)
                intent.putExtra("date", currentDate)
                startActivity(intent)
            }
        })

        calendarView.setOnMonthChangeListener(object : OnMonthChangeListener() {
            override fun onMonthChange(year: Int, month: Int) {
                val dateText = findViewById<TextView>(R.id.calendar_month_text)
                val monthString = DateFormatSymbols().getMonths()[month-1]
                dateText.setText("$monthString - $year")
            }
        })
        /*
        calendarView.setOnDateChangeListener{ view: CalendarView, year: Int, month: Int, day: Int ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            val sdf = SimpleDateFormat("dd.MM.yyyy")
            val currentDate = sdf.format(calendar.timeInMillis)
            Log.d("CALENDAR", "The following date was selected: $currentDate")
            val intent = Intent(this, RatingActivity::class.java)
            intent.putExtra("date", currentDate)
            startActivity(intent)
        }
         */
    }

    override fun onResume(){
        super.onResume()
        markDates()
        val sdf = SimpleDateFormat("MMMM - yyyy")
        val currentDate = sdf.format(Date())
        val dateText = findViewById<TextView>(R.id.calendar_month_text)
        dateText.setText(currentDate)
    }

    fun markDates(){
        val dbHelper = DatabaseHelper(this)
        val dates = dbHelper.getAllRatings()
        val calendarView =  findViewById<MCalendarView>(R.id.calendar_view)
        val prevMarks =  calendarView.markedDates.all.toMutableList()

        val iterator = prevMarks.iterator()
        while (iterator.hasNext()){
            val item = iterator.next()
            calendarView.unMarkDate(item)
        }

        for (date in dates){
            calendarView.markDate(
                DateData(date.year, date.month, date.day).setMarkStyle(MarkStyle(MarkStyle.BACKGROUND, Color.parseColor(date.color)))
            )
        }
    }
}