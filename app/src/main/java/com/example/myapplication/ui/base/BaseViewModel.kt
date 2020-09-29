package com.example.myapplication.ui.base

import androidx.lifecycle.ViewModel

open class BaseViewModel<N : BaseNavigator> : ViewModel() {

    private lateinit var mNavigator: N

    fun setNavigator(navigator: N) {
        this.mNavigator = navigator
    }

    fun getNavigator(): N = mNavigator



}
