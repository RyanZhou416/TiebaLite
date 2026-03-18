package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

object PersonalizedRepository {

    @Volatile
    private var cachedResponse: PersonalizedResponse? = null

    /**
     * 个性推荐
     *
     * @param loadType 加载类型（1 - 下拉刷新 2 - 加载更多）
     * @param page 分页页码
     */
    fun personalizedFlow(loadType: Int, page: Int = 1) = flow {
        emitAll(
            TiebaApi.getInstance()
                .personalizedProtoFlow(loadType, page)
                .map { response ->
                    val liveThreadIds =
                        response.data_?.thread_list?.filter { it.ala_info != null }
                            ?.map { it.id } ?: emptyList()
                    val filtered = response.copy(
                        data_ = response.data_?.copy(
                            thread_list = response.data_.thread_list.filter {
                                !liveThreadIds.contains(it.id)
                            },
                            thread_personalized = response.data_.thread_personalized.filter {
                                !liveThreadIds.contains(it.tid)
                            }
                        )
                    )
                    if (loadType == 1 && page == 1) {
                        cachedResponse = filtered
                    }
                    filtered
                }
        )
    }

    fun getCachedResponse(): PersonalizedResponse? = cachedResponse
}
