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
import com.huanchengfly.tieba.post.utils.PerformanceTracker
import java.util.concurrent.ConcurrentHashMap

object ThreadDetailPrefetchManager {

    private const val TAG = "ThreadPrefetch"
    private const val MAX_CACHE_SIZE = 20
    private const val MAX_CONCURRENCY = 3
    private const val CACHE_TTL_MS = 5 * 60 * 1000L

    private data class CacheEntry(
        val response: PbPageResponse,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired() = System.currentTimeMillis() - timestamp > CACHE_TTL_MS
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val semaphore = Semaphore(MAX_CONCURRENCY)
    private val inFlight = ConcurrentHashMap<Long, Job>()

    private val cache = object : LinkedHashMap<Long, CacheEntry>(
        MAX_CACHE_SIZE + 1, 0.75f, true
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, CacheEntry>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }

    @Volatile
    var enabled: Boolean = true

    fun prefetch(threadId: Long, forumId: Long? = null) {
        if (!enabled) return
        synchronized(cache) {
            val existing = cache[threadId]
            if (existing != null && !existing.isExpired()) return
        }
        if (inFlight.containsKey(threadId)) return

        PerformanceTracker.recordPrefetchRequest()
        val job = scope.launch {
            semaphore.withPermit {
                try {
                    val response = PbPageRepository
                        .pbPage(threadId, page = 1, forumId = forumId)
                        .firstOrNull()
                    if (response != null) {
                        synchronized(cache) {
                            cache[threadId] = CacheEntry(response)
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
            val entry = cache[threadId] ?: return null
            if (entry.isExpired()) {
                cache.remove(threadId)
                PerformanceTracker.recordPrefetchMiss()
                return null
            }
            PerformanceTracker.recordPrefetchHit()
            return entry.response
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
