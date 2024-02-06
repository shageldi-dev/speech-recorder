package com.bnyro.recorder.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecord(record: RecordEntity)

    @Query("SELECT * FROM records ORDER BY id DESC")
    fun getAllRecords(): List<RecordEntity>?

    @Delete
    suspend fun deleteRecord(record: RecordEntity)

    @Query("DELETE FROM records WHERE filename=:filename")
    suspend fun deleteRecord(filename: String)

    @Query("DELETE FROM records WHERE 1=1")
    suspend fun deleteAll()

    @Query("UPDATE records SET filesize=:filesize WHERE filename=:filename")
    suspend fun update(filename: String, filesize: Long)

    // Add more queries as needed
}


