package com.example.myapplication.ui.main

import com.example.myapplication.data.repository.NoteRepository
import com.example.myapplication.ui.main.add_note.AddNoteViewModel
import com.example.myapplication.ui.main.view_note.ViewNoteViewModel
import dagger.Module
import dagger.Provides

@Module
class MainModule {
    @Provides
    fun provideMainActivityViewModel(noteRepository: NoteRepository): MainActivityViewModel {
        return MainActivityViewModel(noteRepository = noteRepository)
    }

    @Provides
    fun provideAddNoteViewModel(noteRepository: NoteRepository): AddNoteViewModel {
        return AddNoteViewModel(noteRepository)
    }

    @Provides
    fun provideViewNoteViewModel(noteRepository: NoteRepository): ViewNoteViewModel {
        return ViewNoteViewModel(noteRepository)
    }
}