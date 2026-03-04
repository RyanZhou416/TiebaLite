package com.huanchengfly.tieba.post.models.database

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "searchposthistory",
    indices = [Index(value = ["content"], unique = true)]
)
data class SearchPostHistory(
    val content: String,
    @ColumnInfo(name = "forumname")
    val forumName: String,
    val timestamp: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
)
