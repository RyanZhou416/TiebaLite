package com.huanchengfly.tieba.post.models.database

import android.content.Context
import androidx.room.Room
import com.huanchengfly.tieba.post.models.database.dao.AccountDao
import com.huanchengfly.tieba.post.models.database.dao.BlockDao
import com.huanchengfly.tieba.post.models.database.dao.DraftDao
import com.huanchengfly.tieba.post.models.database.dao.HistoryDao
import com.huanchengfly.tieba.post.models.database.dao.SearchHistoryDao
import com.huanchengfly.tieba.post.models.database.dao.SearchPostHistoryDao
import com.huanchengfly.tieba.post.models.database.dao.TopForumDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_37_38)
            .build()
    }

    @Provides
    fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideBlockDao(db: AppDatabase): BlockDao = db.blockDao()

    @Provides
    fun provideDraftDao(db: AppDatabase): DraftDao = db.draftDao()

    @Provides
    fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao()

    @Provides
    fun provideSearchHistoryDao(db: AppDatabase): SearchHistoryDao = db.searchHistoryDao()

    @Provides
    fun provideSearchPostHistoryDao(db: AppDatabase): SearchPostHistoryDao = db.searchPostHistoryDao()

    @Provides
    fun provideTopForumDao(db: AppDatabase): TopForumDao = db.topForumDao()
}
