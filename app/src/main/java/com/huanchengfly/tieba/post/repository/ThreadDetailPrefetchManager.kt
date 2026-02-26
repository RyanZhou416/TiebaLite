package com.huanchengfly.tieba.post.repository

import android.util.Log
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.ConcurrentHashMap

object ThreadDetailPrefetchManager {

    private const val TAG = "ThreadPrefetch"
    private const val MAX_CACHE_SIZE = 15
    private const val MAX_CONCURRENCY = 2

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val semaphore = Semaphore(MAX_CONCURRENCY)
    private val inFlight = ConcurrentHashMap<Long, Job>()

    private val cache = object : LinkedHashMap<Long, PbPageResponse>(
        MAX_CACHE_SIZE + 1, 0.75f, true
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, PbPageResponse>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }

    @Volatile
    var enabled: Boolean = true

    fun prefetch(threadId: Long, forumId: Long? = null) {
        if (!enabled) return
        synchronized(cache) {
            if (cache.containsKey(threadId)) return
        }
        if (inFlight.containsKey(threadId)) return

        val job = scope.launch {
            semaphore.withPermit {
                try {
                    val response = PbPageRepository
                        .pbPage(threadId, page = 1, forumId = forumId)
                        .firstOrNull()
                    if (response != null) {
                        synchronized(cache) {
                            cache[threadId] = response
                        }
                        Log.d(TAG, "Prefetched threadId=$threadId")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Prefetch failed threadId=$threadId: ${e.message}")
                } finally {
                    inFlight.remove(threadId)
                }
            }
        }
        inFlight[threadId] = job
    }

    fun get(threadId: Long): PbPageResponse? {
        synchronized(cache) {
            return cache.remove(threadId)
        }
    }

    fun evict(threadId: Long) {
        synchronized(cache) {
            cache.remove(threadId)
        }
        inFlight.remove(threadId)?.cancel()
    }

    fun clear() {
        synchronized(cache) {
            cache.clear()
        }
        inFlight.values.forEach { it.cancel() }
        inFlight.clear()
    }
}
