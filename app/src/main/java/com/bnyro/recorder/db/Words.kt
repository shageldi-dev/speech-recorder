package com.bnyro.recorder.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Words(
    @PrimaryKey()
    @ColumnInfo(name = "word")
    val word: String,

    @ColumnInfo(name = "date")
    val date: Long = System.currentTimeMillis(), // Assuming date is a timestamp in milliseconds

    @ColumnInfo(name = "index")
    val index: Int,
)