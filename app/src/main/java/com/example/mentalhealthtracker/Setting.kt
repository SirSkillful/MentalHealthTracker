package com.example.mentalhealthtracker

class Setting(val text: String, val rating: Int, val color: String) {

    fun getRatingInt(): Int{
        return rating
    }

    fun getTextString(): String{
        return text
    }

    fun getColorString(): String{
        return color
    }
}