package com.example.myapplication.ui.main.view_note

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.db.entity.Note
import com.example.myapplication.data.repository.NoteRepository
import com.example.myapplication.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class ViewNoteViewModel(private val noteRepository: NoteRepository) :
    BaseViewModel<ViewNoteNavigator>() {

    fun getNoteById(id: Int): LiveData<Note> {
        return noteRepository.getNoteById(id)

    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }


}