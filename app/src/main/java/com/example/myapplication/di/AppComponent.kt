package com.example.myapplication.di

import com.example.myapplication.MainActivity
import com.example.myapplication.di.module.AddNoteViewModelModule
import com.example.myapplication.di.module.AppModule
import com.example.myapplication.di.module.MainActivityViewModelModule
import com.example.myapplication.di.di_utils.ViewModelFactoryModule
import com.example.myapplication.di.module.ViewNoteViewModelModule
import com.example.myapplication.ui.adapter.AdaptersModule
import com.example.myapplication.ui.main.add_note.AddNoteActivity
import com.example.myapplication.ui.main.MainModule
import com.example.myapplication.ui.main.view_note.ViewNoteActivity
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        AppModule::class,
        MainModule::class,
        AddNoteViewModelModule::class,
        MainActivityViewModelModule::class,
        ViewModelFactoryModule::class,
        ViewNoteViewModelModule::class,
        AdaptersModule::class]
)
interface AppComponent {

    fun inject(activity: MainActivity)

    fun inject(activity: AddNoteActivity)

    fun inject(activity: ViewNoteActivity)
}