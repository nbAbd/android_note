package com.example.myapplication.data.local.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.data.local.db.entity.Note

@Dao
interface NoteDao {

    @Insert
    suspend fun insertNote(note: Note)

    @Query("SELECT * FROM NOTE_TABLE")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT *FROM NOTE_TABLE WHERE ID= :note_id")
    fun getNoteById(note_id: Int): LiveData<Note>

    @Update
    suspend fun updateNote(note: Note)

    @Query("DELETE FROM NOTE_TABLE WHERE id = :note_id")
    suspend fun deleteNoteById(note_id: Int)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM NOTE_TABLE WHERE ID IN (:selectedNotesId)")
    suspend fun deleteMultipleNotesById(selectedNotesId: List<Int?>)


}