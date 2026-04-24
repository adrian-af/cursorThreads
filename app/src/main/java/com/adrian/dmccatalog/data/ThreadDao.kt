package com.adrian.dmccatalog.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreadDao {
    @Query("SELECT * FROM threads ORDER BY code")
    fun observeAll(): Flow<List<ThreadEntity>>

    @Query("SELECT * FROM threads WHERE owned = 1 ORDER BY code")
    fun observeOwned(): Flow<List<ThreadEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ThreadEntity>)

    @Update
    suspend fun update(item: ThreadEntity)
}
