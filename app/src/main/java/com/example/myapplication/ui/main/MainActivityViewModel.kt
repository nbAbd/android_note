package com.example.myapplication.ui.main

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.NoteRepository
import com.example.myapplication.ui.base.BaseViewModel
import com.example.myapplication.utils.AppUtils
import kotlinx.coroutines.launch

class MainActivityViewModel(private val noteRepository: NoteRepository) :
    BaseViewModel<MainActivityNavigator>() {

    val notesList = noteRepository.getAllNotes()

    fun deleteMultipleNotesById(selectedIds: List<Int?>) {
        viewModelScope.launch {
            noteRepository.deleteMultipleNotesById(selectedIds)
            notesList.value?.forEach { note ->
                if (selectedIds.contains(note.id)) {
                    note.imgUri?.let { uri ->
                        if (uri.toUri().isAbsolute) {
                            Log.i(javaClass.simpleName, "deleteMultipleNotesById: $uri")
                            AppUtils.deleteImageFromUri(uri = uri)
                        }
                    }
                }
            }

        }
    }
}