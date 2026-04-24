package com.adrian.dmccatalog.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ThreadEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun threadDao(): ThreadDao
}
