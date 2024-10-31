package com.colorcall.callerscreen.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ContactDao {
    @Insert
    suspend fun insertContact(contact: Contact)

    @Query("SELECT * FROM contact")
    suspend fun getAllContacts(): List<Contact>

    @Query("DELETE FROM contact WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("SELECT * FROM contact WHERE background_path = :backgroundPath")
    fun getContactsByBackgroundPath(backgroundPath: String): LiveData<List<Contact>>
    @Query("SELECT * FROM contact WHERE contact_id = :contactId LIMIT 1")
    fun getContactById(contactId: String): Contact?

    @Query("SELECT * FROM contact WHERE contact_id = :contactId")
    suspend fun getContactsByContactId(contactId: String): List<Contact>
    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact)
}
