package com.example.myapplication.ui.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    /***
     * @param position selected/clicked item position
     * @param isActivated  will tell the ViewHolder if the item in that position has been selected by the user or not.
     */
    abstract fun onBind(position: Int, isActivated: Boolean = false)

    fun onBind(position: Int) {}
}
