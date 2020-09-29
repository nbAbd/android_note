package com.example.myapplication.utils

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.adapter.NotesAdapter

class MyItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {

    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as NotesAdapter.NoteViewHolder)
                .getItemDetails()
        }
        return null
    }
}