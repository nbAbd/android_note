package com.example.myapplication.ui.main

import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.NoteRepository
import com.example.myapplication.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class MainActivityViewModel(private val noteRepository: NoteRepository) :
    BaseViewModel<MainActivityNavigator>() {

    val notesList = noteRepository.getAllNotes()

    fun deleteMultipleNotesById(selectedIds: List<Int?>) {
        viewModelScope.launch {
            noteRepository.deleteMultipleNotesById(selectedIds)
        }
    }
}