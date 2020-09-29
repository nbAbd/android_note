package com.example.myapplication.ui.main.add_note

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.db.entity.Note
import com.example.myapplication.data.repository.NoteRepository
import com.example.myapplication.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class AddNoteViewModel(private val noteRepository: NoteRepository) :
    BaseViewModel<AddNoteNavigator>() {

    var note: LiveData<Note>? = null

    fun getNoteById(id: Int): LiveData<Note>? {
        return noteRepository.getNoteById(id)
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
    }

    fun insertNote(note: Note) {
        viewModelScope.launch {
            noteRepository.insertNote(note)
        }
    }


}