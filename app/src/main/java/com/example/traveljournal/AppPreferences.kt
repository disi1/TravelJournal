package com.example.traveljournal

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val NAME = "app_preferences"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences
    private val FIRST_TIME_OPENING_APP = Pair("first_time_opening_app", true)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var firstTimeOpeningApp: Boolean
        get() = preferences.getBoolean(FIRST_TIME_OPENING_APP.first, FIRST_TIME_OPENING_APP.second)
        set(value) = preferences.edit {
            it.putBoolean(FIRST_TIME_OPENING_APP.first, value)
        }
}