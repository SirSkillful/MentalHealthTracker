package com.example.mentalhealthtracker

/**
 * Data structure to represent a rating.
 * @param rating An integer value that represents the rating.
 * @param note A string that holds the notes to a day that a user has written down.
 */
class Rating(val rating: Int, val note: String) {

    /**
     * Return the rating value
     * @return Rating value as integer
     */
    fun getRatingInt(): Int{
        return rating
    }

    /**
     * Get the note from the rating
     * @return Note as a string
     */
    fun getNoteString(): String{
        return note
    }
}