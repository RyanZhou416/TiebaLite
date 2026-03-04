package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.models.database.dao.HistoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

object HistoryUtil {
    const val PAGE_SIZE = 100
    const val TYPE_FORUM = 1
    const val TYPE_THREAD = 2

    lateinit var historyDao: HistoryDao
        private set

    fun initDao(dao: HistoryDao) {
        historyDao = dao
    }

    fun deleteAll() {
        App.appScope.launch(Dispatchers.IO) {
            historyDao.deleteAll()
        }
    }

    @JvmOverloads
    fun saveHistory(history: History, async: Boolean = true) {
        App.appScope.launch(Dispatchers.IO) {
            saveOrUpdate(history)
        }
    }

    fun getFlow(
        type: Int,
        page: Int
    ): Flow<List<History>> {
        return flow {
            emit(historyDao.getByTypePaged(type, PAGE_SIZE, page * PAGE_SIZE))
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun update(history: History): Boolean {
        val historyBean = historyDao.getByData(history.data)
        if (historyBean != null) {
            historyDao.update(
                historyBean.copy(
                    timestamp = System.currentTimeMillis(),
                    title = history.title,
                    extras = history.extras,
                    avatar = history.avatar,
                    username = history.username,
                    count = historyBean.count + 1
                )
            )
            return true
        }
        return false
    }

    private suspend fun saveOrUpdate(history: History) {
        if (update(history)) {
            return
        }
        val saveHistory = history.copy(count = 1, timestamp = System.currentTimeMillis())
        historyDao.insert(saveHistory)
    }

    private fun saveOrUpdateAsync(
        history: History,
        callback: ((Boolean) -> Unit)? = null
    ) {
        App.appScope.launch(Dispatchers.IO) {
            runCatching {
                saveOrUpdate(history)
                callback?.invoke(true)
            }.onFailure {
                callback?.invoke(false)
            }
        }
    }
}
