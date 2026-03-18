package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.BuildConfig
import com.huanchengfly.tieba.post.repository.AdaptivePrefetchManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger

/**
 * Debug 性能追踪器。
 * 收集网络延迟、缓存命中率、预取命中率等指标，供 debug 页面展示和导出。
 *
 * Release 下所有记录方法为 no-op，不分配任何对象，零开销。
 */
object PerformanceTracker {

    private val enabled = BuildConfig.DEBUG

    private val startTime = System.currentTimeMillis()

    // ── 网络延迟 ──
    data class LatencyRecord(val url: String, val durationMs: Long, val timestamp: Long)

    private const val MAX_RECORDS = 100
    private val latencyRecords = ConcurrentLinkedDeque<LatencyRecord>()

    fun recordNetworkLatency(url: String, durationMs: Long) {
        if (!enabled) return
        latencyRecords.addLast(LatencyRecord(url, durationMs, System.currentTimeMillis()))
        while (latencyRecords.size > MAX_RECORDS) latencyRecords.pollFirst()
    }

    // ── 缓存命中 ──
    private val personalizedCacheHits = AtomicInteger(0)
    private val personalizedCacheMisses = AtomicInteger(0)
    private val forumCacheHits = AtomicInteger(0)
    private val forumCacheMisses = AtomicInteger(0)

    fun recordPersonalizedCacheHit() { if (enabled) personalizedCacheHits.incrementAndGet() }
    fun recordPersonalizedCacheMiss() { if (enabled) personalizedCacheMisses.incrementAndGet() }
    fun recordForumCacheHit() { if (enabled) forumCacheHits.incrementAndGet() }
    fun recordForumCacheMiss() { if (enabled) forumCacheMisses.incrementAndGet() }

    // ── 预取命中 ──
    private val prefetchHits = AtomicInteger(0)
    private val prefetchMisses = AtomicInteger(0)
    private val prefetchRequests = AtomicInteger(0)

    fun recordPrefetchRequest() { if (enabled) prefetchRequests.incrementAndGet() }
    fun recordPrefetchHit() { if (enabled) prefetchHits.incrementAndGet() }
    fun recordPrefetchMiss() { if (enabled) prefetchMisses.incrementAndGet() }

    // ── 页面加载 ──
    data class PageLoadRecord(val page: String, val durationMs: Long, val timestamp: Long)

    private val pageLoadRecords = ConcurrentLinkedDeque<PageLoadRecord>()

    fun recordPageLoad(page: String, durationMs: Long) {
        if (!enabled) return
        pageLoadRecords.addLast(PageLoadRecord(page, durationMs, System.currentTimeMillis()))
        while (pageLoadRecords.size > MAX_RECORDS) pageLoadRecords.pollFirst()
    }

    private val pageLoadStartTimes = java.util.concurrent.ConcurrentHashMap<String, Long>()

    fun markPageLoadStart(page: String) {
        if (!enabled) return
        pageLoadStartTimes[page] = System.currentTimeMillis()
    }

    fun markPageLoadEnd(page: String) {
        if (!enabled) return
        val start = pageLoadStartTimes.remove(page) ?: return
        recordPageLoad(page, System.currentTimeMillis() - start)
    }

    // ── 导出报告 ──
    fun generateReport(): String = buildString {
        val now = System.currentTimeMillis()
        val uptimeMin = (now - startTime) / 60000
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        appendLine("═══ TiebaLite Performance Report ═══")
        appendLine("Time: ${dateFormat.format(Date(now))}")
        appendLine("Uptime: ${uptimeMin}min")
        appendLine()

        appendLine("── Network Tier ──")
        appendLine("Current tier: ${AdaptivePrefetchManager.networkTier}")
        appendLine("Avg latency: ${AdaptivePrefetchManager.averageLatency}ms")
        appendLine("→ preloadCount: ${AdaptivePrefetchManager.preloadCount}")
        appendLine("→ prefetchAhead: ${AdaptivePrefetchManager.prefetchAhead}")
        appendLine("→ pageThreadCount: ${AdaptivePrefetchManager.pageThreadCount}")
        appendLine()

        appendLine("── Cache Hit Rate ──")
        val pTotal = personalizedCacheHits.get() + personalizedCacheMisses.get()
        val pRate = if (pTotal > 0) personalizedCacheHits.get() * 100 / pTotal else 0
        appendLine("Personalized: ${personalizedCacheHits.get()}/${pTotal} (${pRate}%)")

        val fTotal = forumCacheHits.get() + forumCacheMisses.get()
        val fRate = if (fTotal > 0) forumCacheHits.get() * 100 / fTotal else 0
        appendLine("Forum: ${forumCacheHits.get()}/${fTotal} (${fRate}%)")
        appendLine()

        appendLine("── Thread Prefetch ──")
        appendLine("Requests: ${prefetchRequests.get()}")
        val pfTotal = prefetchHits.get() + prefetchMisses.get()
        val pfRate = if (pfTotal > 0) prefetchHits.get() * 100 / pfTotal else 0
        appendLine("Hit rate: ${prefetchHits.get()}/${pfTotal} (${pfRate}%)")
        appendLine()

        appendLine("── Recent Network Latencies (last ${latencyRecords.size}) ──")
        val records = latencyRecords.toList()
        if (records.isNotEmpty()) {
            val avg = records.map { it.durationMs }.average().toLong()
            val min = records.minOf { it.durationMs }
            val max = records.maxOf { it.durationMs }
            val p50 = records.map { it.durationMs }.sorted().let { it[it.size / 2] }
            val p95 = records.map { it.durationMs }.sorted().let { it[(it.size * 0.95).toInt().coerceAtMost(it.size - 1)] }
            appendLine("avg=${avg}ms  min=${min}ms  max=${max}ms  p50=${p50}ms  p95=${p95}ms")
            appendLine()
            appendLine("Last 10 requests:")
            records.takeLast(10).forEach { r ->
                val shortUrl = r.url.substringAfter("://").let {
                    if (it.length > 60) it.take(57) + "..." else it
                }
                appendLine("  ${r.durationMs}ms  $shortUrl")
            }
        } else {
            appendLine("(no data)")
        }
        appendLine()

        appendLine("── Recent Page Loads (last ${pageLoadRecords.size}) ──")
        val pages = pageLoadRecords.toList()
        if (pages.isNotEmpty()) {
            pages.takeLast(10).forEach { r ->
                appendLine("  ${r.durationMs}ms  ${r.page}")
            }
        } else {
            appendLine("(no data)")
        }

        appendLine()
        appendLine("═══ End of Report ═══")
    }

    fun reset() {
        latencyRecords.clear()
        personalizedCacheHits.set(0)
        personalizedCacheMisses.set(0)
        forumCacheHits.set(0)
        forumCacheMisses.set(0)
        prefetchHits.set(0)
        prefetchMisses.set(0)
        prefetchRequests.set(0)
        pageLoadRecords.clear()
        pageLoadStartTimes.clear()
    }
}
