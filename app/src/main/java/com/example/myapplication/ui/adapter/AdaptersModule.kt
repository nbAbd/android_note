package com.example.myapplication.ui.adapter

import dagger.Module
import dagger.Provides

@Module
class AdaptersModule {

    @Provides
    fun provideNotesAdapter(): NotesAdapter {
        return NotesAdapter()
    }
}