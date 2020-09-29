package com.example.myapplication.data.local

import android.content.Context
import android.content.SharedPreferences

class AppPreferencesHelper(context: Context) {

    private val mPrefs: SharedPreferences =
        context.getSharedPreferences("DemoNotePrefs", Context.MODE_PRIVATE)

    fun isGrid() = mPrefs.getBoolean("isGrid", true)

    fun setGrid(status: Boolean) {
        mPrefs.edit().putBoolean("isGrid", status).apply()
    }

}