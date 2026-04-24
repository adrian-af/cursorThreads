package com.adrian.dmccatalog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "threads")
data class ThreadEntity(
    @PrimaryKey val code: String,
    val name: String,
    val hex: String,
    val owned: Boolean = false,
    val skeins: Int = 1,
    val notes: String = ""
)
