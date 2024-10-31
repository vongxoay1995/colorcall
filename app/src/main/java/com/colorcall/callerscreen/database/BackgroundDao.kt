package com.colorcall.callerscreen.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BackgroundDao {
    @Insert
    suspend fun insertBackground(background: Background)

    @Query("SELECT * FROM background")
    fun getAllBackgrounds(): LiveData<List<Background>>

    @Query("DELETE FROM background WHERE id = :id")
    suspend fun deleteBackgroundById(id: Long)

    @Delete
    suspend fun delete(background: Background)

    @Query("DELETE FROM background WHERE id = :id")
    suspend fun deleteById(id: Long)

}
