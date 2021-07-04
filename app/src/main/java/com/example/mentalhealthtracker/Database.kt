package com.example.mentalhealthtracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast

private const val SETTINGS_TABLE_NAME = "settings"
private const val SETTINGS_TEXT_COLUMN = "text"
private const val SETTINGS_RATING_COLUMN = "rating"
private const val SETTINGS_COLOR_COLUMN = "color"
private const val RATINGS_TABLE_NAME = "ratings"
private const val RATINGS_DATE_COLUMN = "date"
private const val RATINGS_RATING_COLUMN = "rating"
private const val RATINGS_NOTE_COLUMN = "note"

private const val CREATE_SETTINGS_TABLE =
    "CREATE TABLE $SETTINGS_TABLE_NAME (" +
            "$SETTINGS_RATING_COLUMN INTEGER PRIMARY KEY," +
            "$SETTINGS_TEXT_COLUMN TEXT," +
            "$SETTINGS_COLOR_COLUMN TEXT)"

private const val DELETE_SETTINGS_TABLE =
    "DROP TABLE IF EXISTS $SETTINGS_TABLE_NAME"

private const val CREATE_RATINGS_TABLE =
    "CREATE TABLE $RATINGS_TABLE_NAME (" +
            "$RATINGS_DATE_COLUMN TEXT PRIMARY KEY," +
            "$RATINGS_RATING_COLUMN TEXT," +
            "$RATINGS_NOTE_COLUMN TEXT)"

private const val DELETE_RATINGS_TABLE =
    "DROP TABLE IF EXISTS $RATINGS_TABLE_NAME"

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    var db = this.writableDatabase

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_SETTINGS_TABLE)
        db?.execSQL(CREATE_RATINGS_TABLE)
        this.db = db
        Log.d("CREATION", "OnCreate() has been executed")
        populateSettings()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(DELETE_SETTINGS_TABLE)
        db?.execSQL(DELETE_RATINGS_TABLE)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    private fun populateSettings(){
        val texts = arrayOf<String>("Awful", "Meh", "Neutral","Good", "Amazing")
        val colors = arrayOf<String>("#d10000", "#ff6a00", "#ffc400", "#d4ff00", "#04d600")
        val ratings = arrayOf<Int>(1,2,3,4,5)
        for (i in texts.indices){
            addSetting(texts[i], ratings[i], colors[i])
            Log.d("CREATION", "I have tried to add text no $i")
        }
    }

    /**
     * Add a rating if it does not exist yet or update one if that's the case
     * TODO Maybe set a flag in the Rating Activity so this method can be left out by checking the flag if the given value was extracted from the database or is new
     *
     */
    fun saveRating(date: String, rating: Int, note: String): Boolean{
        if (getRating(date) != null){
            return updateRating(date, rating, note)
        } else {
            return addRating(date, rating, note)
        }
    }

    /**
     * Add a new entry to the database
     *
     * @param date Date string, primary key of the entry
     * @param rating Int value that implies the rating of the day
     * @param note String that contains the note for the given day
     * @return Boolean that indicates if the insert was successful
     */
    private fun addRating(date: String, rating: Int, note: String): Boolean{
        val values = ContentValues().apply{
            put(RATINGS_DATE_COLUMN, date)
            put(RATINGS_RATING_COLUMN, rating)
            put(RATINGS_NOTE_COLUMN, note)
        }
        val ret = db?.insert(RATINGS_TABLE_NAME, null, values)
        return ret?.toInt() != -1
    }

    /**
     * Update a rating in the database for a given date
     */
    private fun updateRating(date: String, rating: Int, note: String): Boolean{
        // New rating and note
        val values = ContentValues().apply {
            put(RATINGS_RATING_COLUMN, rating)
            put(RATINGS_NOTE_COLUMN, note)
        }
        //Affected rows
        val selection = "$RATINGS_DATE_COLUMN LIKE ?" // Prepared query
        val selectionArgs = arrayOf(date)
        // Update and get the count of affected rows
        val count = db.update(RATINGS_TABLE_NAME, values, selection, selectionArgs)

        return count > 0
    }

    /**
     * Get the rating for a given date
     */
     fun getRating(date: String): Rating?{
        //Choose what columns are used
        val projection = arrayOf(RATINGS_RATING_COLUMN, RATINGS_NOTE_COLUMN)
        //Prepare the query
        val selection = "$RATINGS_DATE_COLUMN = ?"
        val selectionArgs = arrayOf(date)
        //Execute the query
        val cursor = db.query(
            RATINGS_TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        //Format the output
        var rating:Rating? = null
        with(cursor){
            if (moveToNext()){
                rating = Rating(
                    getInt(getColumnIndexOrThrow(RATINGS_RATING_COLUMN)),
                    getString(getColumnIndexOrThrow(RATINGS_NOTE_COLUMN))
                )
            }
        }
        cursor.close()
        return rating
    }

    fun getAllRatings(): List<Date>{
        val query = "SELECT $RATINGS_DATE_COLUMN, $SETTINGS_COLOR_COLUMN FROM $RATINGS_TABLE_NAME JOIN " +
                "$SETTINGS_TABLE_NAME USING($SETTINGS_RATING_COLUMN)"
        val cursor = db.rawQuery(query, null)
        val dates = mutableListOf<Date>()
        with(cursor){
            while(moveToNext()){
                val date = getString(getColumnIndexOrThrow(RATINGS_DATE_COLUMN))
                val dateParts = date.split('.')
                val day = dateParts[0].toInt()
                val month = dateParts[1].toInt()
                val year = dateParts[2].toInt()
                val color = getString(getColumnIndexOrThrow(SETTINGS_COLOR_COLUMN))
                dates.add(Date(year, month, day, color))
            }
        }
        return dates
    }

    /**
     * Update the entry in the database where the rating is the one given
     */
    fun updateSetting(text: String, rating: Int, color: String): Boolean{
        // New rating and note
        val values = ContentValues().apply {
            put(SETTINGS_TEXT_COLUMN, text)
            put(SETTINGS_COLOR_COLUMN, color)
        }
        //Affected rows
        val selection = "$SETTINGS_RATING_COLUMN LIKE ?" // Prepared query
        val selectionArgs = arrayOf(rating.toString())
        // Update and get the count of affected rows
        val count = db.update(SETTINGS_TABLE_NAME, values, selection, selectionArgs)

        return count > 0
    }

    fun addSetting(text: String, rating: Int, color: String): Boolean{
        val values = ContentValues().apply{
            put(SETTINGS_TEXT_COLUMN, text)
            put(SETTINGS_RATING_COLUMN, rating)
            put(SETTINGS_COLOR_COLUMN, color)
        }
        val ret = db?.insert(SETTINGS_TABLE_NAME, null, values)
        return ret?.toInt() != -1
    }

    /**
     * Return a list of all settings in the database
     */
    fun getSettings(): List<String>{
        val projection = arrayOf(SETTINGS_TEXT_COLUMN, SETTINGS_RATING_COLUMN)
        val cursor = db.query(
            SETTINGS_TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )
        var results = mutableListOf<String>()
        with(cursor){
            while (moveToNext()){
                val text = getString(getColumnIndexOrThrow(SETTINGS_TEXT_COLUMN))
                val rating = getInt(getColumnIndexOrThrow(SETTINGS_RATING_COLUMN)).toString()
                val entry = "$text - $rating"
                results.add(entry)
            }
        }
        cursor.close()
        return results
    }

    /**
     * Get the text string for a rating if the rating is loaded again.
     * This can be used to set the currently shown spinner value
     */
    fun getSetting(rating: Int): Setting?{
        val projection = arrayOf(SETTINGS_TEXT_COLUMN, SETTINGS_RATING_COLUMN, SETTINGS_COLOR_COLUMN)
        val selection = "$SETTINGS_RATING_COLUMN = ?"
        val selectionArgs = arrayOf(rating.toString())
        val cursor = db.query(
            SETTINGS_TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        var setting:Setting? = null
        with(cursor) {
            if (moveToNext()) {
                val text = getString(getColumnIndexOrThrow(SETTINGS_TEXT_COLUMN))
                val rating = getInt(getColumnIndexOrThrow(SETTINGS_RATING_COLUMN))
                val color = getString(getColumnIndexOrThrow(SETTINGS_COLOR_COLUMN))
                setting = Setting(text, rating, color)
            }
        }
        return setting
    }

    /**
     * Delete a setting and all
     */
    fun deleteSetting(rating: Int): Boolean{
        //Delete the setting
        val selection = "$SETTINGS_RATING_COLUMN LIKE ?"
        val selectionArgs = arrayOf(rating.toString())
        val deletedRows = db.delete(SETTINGS_TABLE_NAME, selection, selectionArgs)
        //Delete all entries where the old rating was used
        val ratingSelection = "$RATINGS_RATING_COLUMN LIKE ?"
        val ratingSelectionArgs = arrayOf(rating.toString())
        db.delete(RATINGS_TABLE_NAME, ratingSelection, ratingSelectionArgs)
        //Return if the operation is successful
        return deletedRows > 0
    }

    fun resetHistory(){
        val db = this.writableDatabase
        db?.execSQL(DELETE_RATINGS_TABLE)
        db?.execSQL(CREATE_RATINGS_TABLE)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "MHT.db"
    }
}
