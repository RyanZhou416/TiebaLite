package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.threadList.ThreadListResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.utils.appPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object FrsPageRepository {
    private const val MAX_CACHE_SIZE = 8

    private val responseCache = object : LinkedHashMap<String, FrsPageResponse>(
        MAX_CACHE_SIZE + 1, 0.75f, true
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, FrsPageResponse>?): Boolean =
            size > MAX_CACHE_SIZE
    }

    fun getCachedResponse(forumName: String, sortType: Int, goodClassifyId: Int?): FrsPageResponse? {
        val key = "${forumName}_${sortType}_${goodClassifyId}"
        return synchronized(responseCache) { responseCache[key] }
    }

    fun frsPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        goodClassifyId: Int? = null,
    ): Flow<FrsPageResponse> =
        TiebaApi.getInstance()
            .frsPage(forumName, page, loadType, sortType, goodClassifyId)
            .map { response ->
                if (response.data_ == null) throw TiebaUnknownException
                val userMap = response.data_.user_list.associateBy { it.id }
                val threadList = response.data_.thread_list
                    .map { threadInfo ->
                        threadInfo.copy(author = userMap[threadInfo.authorId])
                    }
                    .filter { !App.INSTANCE.appPreferences.blockVideo || it.videoInfo == null }
                    .filter { it.ala_info == null }
                val result = response.copy(data_ = response.data_.copy(thread_list = threadList))
                if (page == 1) {
                    val key = "${forumName}_${sortType}_${goodClassifyId}"
                    synchronized(responseCache) { responseCache[key] = result }
                }
                result
            }

    fun threadList(
        forumId: Long,
        forumName: String,
        page: Int,
        sortType: Int,
        threadIds: String = "",
    ): Flow<ThreadListResponse> =
        TiebaApi.getInstance()
            .threadList(forumId, forumName, page, sortType, threadIds)
            .map { response ->
                if (response.data_ == null) throw TiebaUnknownException
                val userMap = response.data_.user_list.associateBy { it.id }
                val threadList = response.data_.thread_list
                    .map { threadInfo ->
                        threadInfo.copy(author = userMap[threadInfo.authorId])
                    }
                    .filter { !App.INSTANCE.appPreferences.blockVideo || it.videoInfo == null }
                    .filter { it.ala_info == null }
                response.copy(data_ = response.data_.copy(thread_list = threadList))
            }
}
