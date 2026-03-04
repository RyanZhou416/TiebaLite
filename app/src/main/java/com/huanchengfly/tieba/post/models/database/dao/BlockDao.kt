package com.huanchengfly.tieba.post.models.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.huanchengfly.tieba.post.models.database.Block

@Dao
interface BlockDao {
    @Query("SELECT * FROM block")
    suspend fun getAll(): List<Block>

    @Insert
    suspend fun insert(block: Block): Long

    @Query("DELETE FROM block WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Insert
    fun insertSync(block: Block): Long
}
