package com.colorcall.callerscreen.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "background")
data class Background(

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    var id: Long? = null,

    @SerializedName("type")
    @ColumnInfo(name = "type")
    var type: Int,

    @SerializedName("path-thumb")
    @NonNull
    @ColumnInfo(name = "path_thumb")
    var pathThumb: String="",

    @SerializedName("path-file")
    @NonNull
    @ColumnInfo(name = "path_item")
    var pathItem: String="",

    @SerializedName("delete")
    @ColumnInfo(name = "delete")
    var delete: Boolean,

    @SerializedName("name")
    @NonNull
    @ColumnInfo(name = "name")
    var name: String="",

    @SerializedName("time_update")
    @NonNull
    @ColumnInfo(name = "time_update")
    var timeUpdate: String="",

    @SerializedName("position")
    @ColumnInfo(name = "position")
    var position: Int = 0
) {
    constructor(type: Int, pathThumb: String, pathItem: String, delete: Boolean) : this(
        id = null,
        type = type,
        pathThumb = pathThumb,
        pathItem = pathItem,
        delete = delete,
        name = "",
        timeUpdate = "",
        position = 0
    )
    constructor(type: Int, pathThumb: String, pathItem: String, delete: Boolean, name: String, position: Int) : this(
        id = null,
        type = type,
        pathThumb = pathThumb,
        pathItem = pathItem,
        delete = delete,
        name = name,
        timeUpdate = "",
        position = position
    )

    constructor(type: Int, pathThumb: String, pathItem: String, delete: Boolean, name: String) : this(
        id = null,
        type = type,
        pathThumb = pathThumb,
        pathItem = pathItem,
        delete = delete,
        name = name,
        timeUpdate = "",
        position = 0
    )
    constructor(id: Long?, type: Int, pathThumb: String, pathItem: String, delete: Boolean, name: String) : this(
        id = id,
        type = type,
        pathThumb = pathThumb,
        pathItem = pathItem,
        delete = delete,
        name = name,
        timeUpdate = "",
        position = 0
    )

    constructor(id: Long?, type: Int, pathThumb: String, pathItem: String, delete: Boolean) : this(
        id = id,
        type = type,
        pathThumb = pathThumb,
        pathItem = pathItem,
        delete = delete,
        name = "",
        timeUpdate = "",
        position = 0
    )

    override fun toString(): String {
        return "Background(id=$id, type=$type, pathThumb='$pathThumb', pathItem='$pathItem', delete=$delete, name='$name', timeUpdate='$timeUpdate', position=$position)"
    }
}

