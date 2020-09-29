package com.example.myapplication.di.module

import androidx.lifecycle.ViewModel
import com.example.myapplication.di.di_utils.ViewModelKey
import com.example.myapplication.ui.main.view_note.ViewNoteViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewNoteViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(ViewNoteViewModel::class)
    abstract fun bindAddNoteViewModel(viewModel: ViewNoteViewModel): ViewModel
}