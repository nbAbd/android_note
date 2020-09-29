package com.example.myapplication.ui.base

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {

    fun showToast(message: String) {
        activity?.let {
            if (it is BaseActivity) {
                it.showToast(message)
            } else {
                Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showToast(@StringRes message: Int) {
        activity?.let {
            if (it is BaseActivity) {
                it.showToast(message)
            } else {
                Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
            }
        }
    }


}