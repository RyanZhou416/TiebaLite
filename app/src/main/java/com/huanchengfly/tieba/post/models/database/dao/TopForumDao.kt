package com.huanchengfly.tieba.post.models.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.huanchengfly.tieba.post.models.database.TopForum

@Dao
interface TopForumDao {
    @Query("SELECT * FROM topforum")
    suspend fun getAll(): List<TopForum>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(topForum: TopForum)

    @Query("DELETE FROM topforum WHERE forumid = :forumId")
    suspend fun deleteByForumId(forumId: String): Int
}
