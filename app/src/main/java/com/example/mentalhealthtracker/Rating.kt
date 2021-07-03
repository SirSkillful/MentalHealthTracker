package com.example.mentalhealthtracker

class Rating(val rating: Int, val note: String) {

    fun getRatingInt(): Int{
        return rating
    }

    fun getNoteString(): String{
        return note
    }
}