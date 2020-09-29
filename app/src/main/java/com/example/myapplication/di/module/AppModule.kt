package com.example.myapplication.di.module

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.AppPreferencesHelper
import com.example.myapplication.data.repository.NoteRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val mApplication: Application) {

    @Provides
    fun provideContext(): Context = mApplication

    @Provides
    @Singleton
    internal fun provideAppDatabase(context: Context) =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java, "notes_db"
        )
            .build()

    @Provides
    @Singleton
    fun provideNoteRepository(appDatabase: AppDatabase): NoteRepository {
        return NoteRepository(appDatabase)
    }

    @Provides
    @Singleton
    fun provideAppPreferencesHelper(context: Context): AppPreferencesHelper {
        return AppPreferencesHelper(context)
    }
}