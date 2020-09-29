package com.example.myapplication.di.module

import androidx.lifecycle.ViewModel
import com.example.myapplication.di.di_utils.ViewModelKey
import com.example.myapplication.ui.main.MainActivityViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainActivityViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindMainActivityViewModel(viewModel: MainActivityViewModel): ViewModel
}