package com.example.mentalhealthtracker

/**
 * Structure that holds information for a rating option.
 * @param text String that describes the option
 * @param rating Int value that describes the rating. The higher the better
 * @param color String containing a hexcode to color a rating in the history
 */
class Setting(val text: String, val rating: Int, val color: String) {

    /**
     * Get the rating as an int
     * @return Rating as int
     */
    fun getRatingInt(): Int{
        return rating
    }

    /**
     * Get the description text as a string
     * @return Description text as string
     */
    fun getTextString(): String{
        return text
    }

    /**
     * Get the color string
     * @return Color code as a string
     */
    fun getColorString(): String{
        return color
    }
}