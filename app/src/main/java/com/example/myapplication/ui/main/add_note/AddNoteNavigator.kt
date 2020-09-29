package com.example.myapplication.ui.main.add_note

import com.example.myapplication.ui.base.BaseNavigator

interface AddNoteNavigator : BaseNavigator {

    fun onEditNote()

    fun onAddNewNote()

    fun onChooseColorClick()
}