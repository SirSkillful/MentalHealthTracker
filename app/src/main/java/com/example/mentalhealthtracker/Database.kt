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

private const val CREATE_SETTINGS_TABLE = // String that contains the query for creating the settings (rating options) table
    "CREATE TABLE $SETTINGS_TABLE_NAME (" +
            "$SETTINGS_RATING_COLUMN INTEGER PRIMARY KEY," +
            "$SETTINGS_TEXT_COLUMN TEXT," +
            "$SETTINGS_COLOR_COLUMN TEXT)"

private const val DELETE_SETTINGS_TABLE = // String that contains the query for deleting the settings table
    "DROP TABLE IF EXISTS $SETTINGS_TABLE_NAME"

private const val CREATE_RATINGS_TABLE = // String that contains the query for creating the ratings table
    "CREATE TABLE $RATINGS_TABLE_NAME (" +
            "$RATINGS_DATE_COLUMN TEXT PRIMARY KEY," +
            "$RATINGS_RATING_COLUMN TEXT," +
            "$RATINGS_NOTE_COLUMN TEXT)"

private const val DELETE_RATINGS_TABLE = // String that contains the query for deleting the ratings table
    "DROP TABLE IF EXISTS $RATINGS_TABLE_NAME"

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    var db = this.writableDatabase // DB connection instance

    /**
     * Overwritten onCreate method called on installation of the app
     * Initializes the database
     */
    override fun onCreate(db: SQLiteDatabase?) {
        // Create the tables on install of the app
        db?.execSQL(CREATE_SETTINGS_TABLE)
        db?.execSQL(CREATE_RATINGS_TABLE)
        this.db = db
        // Add default rating options to the DB
        populateSettings()
    }

    /**
     * Overwritten default onUpgrade method, called when DB is updated
     * Deletes the tables and then reinstates them
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(DELETE_SETTINGS_TABLE)
        db?.execSQL(DELETE_RATINGS_TABLE)
        onCreate(db)
    }

    /**
     * Overwritten default onDowngrade method
     * Deletes the tables and then reinstates them
     */
    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    /**
     * Method to enter the default rating options to the database
     */
    private fun populateSettings(){
        val texts = arrayOf<String>("Awful", "Meh", "Neutral","Good", "Amazing") // Descriptions of the rating options in order
        val colors = arrayOf<String>("#d10000", "#ff6a00", "#ffc400", "#d4ff00", "#04d600") // Assigned colors in order
        val ratings = arrayOf<Int>(1,2,3,4,5) // Rating values in order
        for (i in texts.indices){
            addSetting(texts[i], ratings[i], colors[i]) // Add rating option to the database
        }
    }

    /**
     * Add a rating if it does not exist yet or update one if that's the case
     * @param date String containing the date in standard european format dd.MM.yyyy
     * @param rating Int holding the rating value
     * @param note String that holds the note text for a day
     * @return True when update/saving succesful, else false
     */
    fun saveRating(date: String, rating: Int, note: String): Boolean{
        if (getRating(date) != null){ // Update an existing rating if one exists
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
        return ret?.toInt() != -1 // Return whether an error occurred with the query execution
    }

    /**
     * Update a rating in the database for a given date
     * @param date Date string, primary key of the entry
     * @param rating Int value that implies the rating of the day
     * @param note String that contains the note for the given day
     * @return Boolean that indicates if the update was succesful
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
        // Return if any rows have been affected
        return count > 0
    }

    /**
     * Get a rating structure from the database containing the rating itself and the note for a given date.
     *
     * @param date Date string, primary key of the entry, structure dd.MM.yyyy
     * @return Rating structure with note and rating value
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
            if (moveToNext()){ // Create a rating from the received information
                rating = Rating(
                    getInt(getColumnIndexOrThrow(RATINGS_RATING_COLUMN)),
                    getString(getColumnIndexOrThrow(RATINGS_NOTE_COLUMN))
                )
            }
        }
        cursor.close()
        return rating
    }

    /**
     * Get ratings for all dates that have one.
     * Contains the date and the color for the according rating
     * Used to color the calender view
     * @return List of all dates with the rating color
     */
    fun getAllRatings(): List<Date>{
        val query = "SELECT $RATINGS_DATE_COLUMN, $SETTINGS_COLOR_COLUMN FROM $RATINGS_TABLE_NAME JOIN " + // Query to the a ratings date and it's color
                "$SETTINGS_TABLE_NAME USING($SETTINGS_RATING_COLUMN)"
        val cursor = db.rawQuery(query, null)
        val dates = mutableListOf<Date>()
        with(cursor){
            while(moveToNext()){ // Add all ratings the cursor contains as a date structure to the list
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
     * @param text String description of the rating option
     * @param rating Int value of the rating
     * @param color String hexcode that is the color of the rating option in the calender view
     * @return True if a rating option has been updated, else false
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
        // Return if a setting has been updated by checking if rows have been affected
        return count > 0
    }

    /**
     * Add a rating option to the database
     * @param text String description of the rating option
     * @param rating Int value of the rating option
     * @param color String hexcode of the color of the rating option
     * @return True if the setting was added successfully
     */
    fun addSetting(text: String, rating: Int, color: String): Boolean{
        val values = ContentValues().apply{
            put(SETTINGS_TEXT_COLUMN, text)
            put(SETTINGS_RATING_COLUMN, rating)
            put(SETTINGS_COLOR_COLUMN, color)
        }
        val ret = db?.insert(SETTINGS_TABLE_NAME, null, values)
        return ret?.toInt() != -1 // Return if the execution of the query was not erroneous
    }

    /**
     * Return a list of all settings/rating option in the database
     * @return List of rating options as formated strings for the use in a spinner
     */
    fun getSettings(): List<String>{
        val projection = arrayOf(SETTINGS_TEXT_COLUMN, SETTINGS_RATING_COLUMN) // We want the description of a rating option and it's integer value
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
            while (moveToNext()){ // Populate the list with all returned ratings in a string format
                val text = getString(getColumnIndexOrThrow(SETTINGS_TEXT_COLUMN))
                val rating = getInt(getColumnIndexOrThrow(SETTINGS_RATING_COLUMN)).toString()
                val entry = "$text - $rating" // Default format for a spinner entry showing the rating option
                results.add(entry)
            }
        }
        cursor.close()
        return results
    }

    /**
     * Get the text string for a rating if the rating is loaded again.
     * This can be used to set the currently shown spinner value
     * @param Int of the rating value, unique identifier for the DB
     * @return A setting structure if one exists for the given rating, else null
     */
    fun getSetting(rating: Int): Setting?{
        val projection = arrayOf(SETTINGS_TEXT_COLUMN, SETTINGS_RATING_COLUMN, SETTINGS_COLOR_COLUMN) // Get text, rating value and color of a rating option
        val selection = "$SETTINGS_RATING_COLUMN = ?" // We want to only select the DB entry where the rating value is like the one passed
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
        var setting:Setting? = null // Initialize as null to check later if a setting was found at all
        with(cursor) {
            if (moveToNext()) { // Create the setting object with the information returned by the query
                val text = getString(getColumnIndexOrThrow(SETTINGS_TEXT_COLUMN))
                val rating = getInt(getColumnIndexOrThrow(SETTINGS_RATING_COLUMN))
                val color = getString(getColumnIndexOrThrow(SETTINGS_COLOR_COLUMN))
                setting = Setting(text, rating, color)
            }
        }
        return setting
    }

    /**
     * Delete a setting and all rating entries were the old setting was used
     * @param rating Int value
     * @return True if the rating entries have been deleted, else false
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
        //Return if the operation is successful (if ratings have been deleted)
        return deletedRows > 0
    }

    /**
     * Delete all history entries by dropping the table and reinstate it
     */
    fun resetHistory(){
        db?.execSQL(DELETE_RATINGS_TABLE)
        db?.execSQL(CREATE_RATINGS_TABLE)
    }

    /**
     * Reset all the rating options to default.
     * Also deletes all the ratings where none of the default values were used because custom ratings
     * that don't use the values had to be adjusted/projected to the default ones, which is not easy,
     * as the user specified the custom ratings and the program has no knowledge about the meaning of
     * how the user interprets the custom ratings.
     * @return True if the rating entries have been deleted, else false
     */
    fun resetSettings(): Boolean{
        db?.execSQL(DELETE_SETTINGS_TABLE)
        db?.execSQL(CREATE_SETTINGS_TABLE)
        populateSettings() // Repopulate the rating options with the default ones
        // Delete all rating entries with wrong rating values
        val selection = "$RATINGS_RATING_COLUMN NOT IN (\"1\",\"2\",\"3\",\"4\",\"5\")" // 1-5 are the default rating values
        val selectionArgs = arrayOf<String>()
        val deletedRows = db.delete(RATINGS_TABLE_NAME, selection, selectionArgs)
        //Return if the operation is successful (if ratings have been deleted)
        return deletedRows > 0
    }

    // Public static variables
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "MHT.db"
    }
}
