package com.example.myapplication.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "note_table")
data class Note(
    var title: String?,
    var content: String?,
    var color: String?,
    var date: String?
) : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

}