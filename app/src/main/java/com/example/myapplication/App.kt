package com.example.myapplication

import android.app.Application
import android.content.Context
import com.example.myapplication.di.AppComponent
import com.example.myapplication.di.DaggerAppComponent
import com.example.myapplication.di.module.AppModule

class App : Application() {

    companion object {
        fun get(context: Context): App = context.applicationContext as App
    }

    private lateinit var mComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        mComponent = initAppComponent()
    }

    private fun initAppComponent(): AppComponent =
        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()

    fun getComponent(): AppComponent = mComponent
}