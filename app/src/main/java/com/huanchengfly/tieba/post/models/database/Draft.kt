package com.huanchengfly.tieba.post.models.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "draft",
    indices = [Index(value = ["hash"], unique = true)]
)
data class Draft(
    val hash: String,
    val content: String,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
)
