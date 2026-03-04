package com.huanchengfly.tieba.post.models.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.huanchengfly.tieba.post.models.database.SearchPostHistory

@Dao
interface SearchPostHistoryDao {
    @Query("SELECT * FROM searchposthistory ORDER BY timestamp DESC")
    suspend fun getAllOrdered(): List<SearchPostHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(history: SearchPostHistory)

    @Query("DELETE FROM searchposthistory WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM searchposthistory")
    suspend fun deleteAll()
}
