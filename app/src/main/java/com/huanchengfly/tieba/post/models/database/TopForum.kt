package com.huanchengfly.tieba.post.models.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "topforum",
    indices = [Index(value = ["forumid"], unique = true)]
)
data class TopForum(
    @ColumnInfo(name = "forumid")
    val forumId: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)
