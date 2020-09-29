package com.example.myapplication.di.module

import androidx.lifecycle.ViewModel
import com.example.myapplication.di.di_utils.ViewModelKey
import com.example.myapplication.ui.main.add_note.AddNoteViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AddNoteViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(AddNoteViewModel::class)
    abstract fun bindAddNoteViewModel(viewModel: AddNoteViewModel): ViewModel
}