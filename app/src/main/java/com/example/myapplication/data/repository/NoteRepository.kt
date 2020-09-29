package com.example.myapplication.data.repository

import androidx.lifecycle.LiveData
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.db.entity.Note

class NoteRepository(private val appDatabase: AppDatabase) {

    suspend fun insertNote(note: Note) {
        appDatabase.noteDao().insertNote(note)
    }

    fun getAllNotes(): LiveData<List<Note>> = appDatabase.noteDao().getAllNotes()

    fun getNoteById(note_id: Int): LiveData<Note> = appDatabase.noteDao().getNoteById(note_id)


    suspend fun updateNote(note: Note) {
        appDatabase.noteDao().updateNote(note)
    }

    suspend fun deleteNote(note: Note) {
        appDatabase.noteDao().deleteNote(note)
    }

    suspend fun deleteNoteById(note_id: Int) {
        appDatabase.noteDao().deleteNoteById(note_id)
    }

    suspend fun deleteMultipleNotesById(selectedIds: List<Int?>) {
        appDatabase.noteDao().deleteMultipleNotesById(selectedIds)
    }
}