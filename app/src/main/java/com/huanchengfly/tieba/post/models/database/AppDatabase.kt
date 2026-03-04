package com.huanchengfly.tieba.post.models.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.huanchengfly.tieba.post.models.database.dao.AccountDao
import com.huanchengfly.tieba.post.models.database.dao.BlockDao
import com.huanchengfly.tieba.post.models.database.dao.DraftDao
import com.huanchengfly.tieba.post.models.database.dao.HistoryDao
import com.huanchengfly.tieba.post.models.database.dao.SearchHistoryDao
import com.huanchengfly.tieba.post.models.database.dao.SearchPostHistoryDao
import com.huanchengfly.tieba.post.models.database.dao.TopForumDao

@Database(
    entities = [
        Account::class,
        Block::class,
        Draft::class,
        History::class,
        SearchHistory::class,
        SearchPostHistory::class,
        TopForum::class,
    ],
    version = 38,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun blockDao(): BlockDao
    abstract fun draftDao(): DraftDao
    abstract fun historyDao(): HistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun searchPostHistoryDao(): SearchPostHistoryDao
    abstract fun topForumDao(): TopForumDao

    companion object {
        const val DATABASE_NAME = "tblite.db"
    }
}
