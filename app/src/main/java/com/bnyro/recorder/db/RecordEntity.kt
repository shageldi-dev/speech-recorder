package com.bnyro.recorder.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "filename")
    val filename: String,

    @ColumnInfo(name = "date")
    val date: Long = System.currentTimeMillis(), // Assuming date is a timestamp in milliseconds

    @ColumnInfo(name = "file_directory")
    val fileDirectory: String,

    @ColumnInfo(name = "words")
    val words: List<String>,

    @ColumnInfo(name = "filesize")
    val filesize: Long
)
