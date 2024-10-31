package com.colorcall.callerscreen.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact")
data class Contact(

    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,

    @NonNull
    @ColumnInfo(name = "contact_id")
    var contactId: String,

    @NonNull
    @ColumnInfo(name = "background_path")
    var backgroundPath: String,

    @NonNull
    @ColumnInfo(name = "background")
    var background: String
){
    // Constructor thứ hai cho phép tạo Contact mà không cần truyền id
    constructor(contactId: String, backgroundPath: String, background: String) : this(
        id = null,  // Room sẽ tự động gán id
        contactId = contactId,
        backgroundPath = backgroundPath,
        background = background
    )
}
