package com.huanchengfly.tieba.post.models.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.huanchengfly.tieba.post.models.database.SearchHistory

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM searchhistory ORDER BY timestamp DESC")
    suspend fun getAllOrdered(): List<SearchHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(history: SearchHistory)

    @Query("DELETE FROM searchhistory WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM searchhistory")
    suspend fun deleteAll()
}
