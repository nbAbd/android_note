package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.local.db.entity.Note
import com.example.myapplication.databinding.NoteSingleItemBinding
import com.example.myapplication.ui.base.BaseViewHolder

class NotesAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    val notesList = mutableListOf<Note>()
    var mTracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    fun setItems(items: List<Note>) {
        this.notesList.clear()
        this.notesList.addAll(items)
        notifyDataSetChanged()
    }


    var onClickListener: OnNoteItemClickListener? = null

    fun setOnNoteItemClickListener(onNoteItemClickListener: OnNoteItemClickListener) {
        this.onClickListener = onNoteItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NoteSingleItemBinding.inflate(inflater, parent, false)
        return NoteViewHolder(binding)
    }


    override fun getItemCount(): Int = notesList.size

    override fun getItemId(position: Int): Long = position.toLong()


    override
    fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        mTracker?.let { holder.onBind(position, it.isSelected(position.toLong())) }
    }

    inner class NoteViewHolder internal constructor(private val binding: NoteSingleItemBinding) :
        BaseViewHolder(binding.root) {

        override fun onBind(position: Int, isActivated: Boolean) {
            binding.item = notesList[position]
            binding.executePendingBindings()

            binding.root.setOnClickListener {
                onClickListener?.onClick(notesList[position])
            }

            binding.root.setOnLongClickListener {
                onClickListener?.onLongClick(position)
                true
            }

            if (isActivated) {
                binding.checkNoteIv.visibility = View.VISIBLE
                binding.checkNoteIv.isActivated = isActivated
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = layoutPosition
                override fun getSelectionKey(): Long? = itemId
            }

    }

    interface OnNoteItemClickListener {
        fun onClick(note: Note)

        fun onLongClick(position: Int)
    }
}