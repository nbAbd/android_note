package com.example.myapplication.di.di_utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.Exception
import java.lang.RuntimeException
import javax.inject.Inject
import javax.inject.Provider

@Suppress("UNCHECKED_CAST")
class ViewModelFactory @Inject constructor(private val viewModelsMap: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val creator = viewModelsMap[modelClass] ?: viewModelsMap.asIterable().firstOrNull {
            modelClass.isAssignableFrom(it.key)
        }?.value ?: throw  IllegalArgumentException("Unknown model class $modelClass")
        return try {
            creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

}