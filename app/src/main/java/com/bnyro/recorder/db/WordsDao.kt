package com.bnyro.recorder.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WordsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: Words)

    @Query("SELECT * FROM words")
    fun getAll(): LiveData<List<Words>>

    @Delete
    suspend fun delete(record: Words)
}