package com.huanchengfly.tieba.post.models.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.huanchengfly.tieba.post.models.database.History

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC, count DESC LIMIT 100")
    suspend fun getAll(): List<History>

    @Query("SELECT * FROM history ORDER BY timestamp DESC, count DESC LIMIT 100")
    fun getAllSync(): List<History>

    @Query("SELECT * FROM history WHERE type = :type ORDER BY timestamp DESC, count DESC LIMIT :limit")
    suspend fun getByType(type: Int, limit: Int = 100): List<History>

    @Query("SELECT * FROM history WHERE type = :type ORDER BY timestamp DESC, count DESC LIMIT :limit")
    fun getByTypeSync(type: Int, limit: Int = 100): List<History>

    @Query("SELECT * FROM history WHERE type = :type ORDER BY timestamp DESC, count DESC LIMIT :limit OFFSET :offset")
    suspend fun getByTypePaged(type: Int, limit: Int, offset: Int): List<History>

    @Query("SELECT * FROM history WHERE data = :data LIMIT 1")
    suspend fun getByData(data: String): History?

    @Query("SELECT * FROM history WHERE data = :data LIMIT 1")
    fun getByDataSync(data: String): History?

    @Insert
    suspend fun insert(history: History): Long

    @Insert
    fun insertSync(history: History): Long

    @Update
    suspend fun update(history: History)

    @Update
    fun updateSync(history: History)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM history")
    suspend fun deleteAll()
}
