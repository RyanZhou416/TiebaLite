package com.huanchengfly.tieba.post.repository

import android.util.Log

/**
 * 自适应预加载管理器。
 *
 * 通过追踪 API 请求的响应延迟，将网络质量分为四档，
 * 并据此动态调整预加载策略：网络越慢，越积极地预加载。
 *
 * 典型场景：国内用户延迟 < 300ms，海外（如加拿大）延迟 800-2000ms+。
 */
object AdaptivePrefetchManager {

    private const val TAG = "AdaptivePrefetch"
    private const val WINDOW_SIZE = 15

    enum class NetworkTier {
        FAST,       // < 300ms — 国内优质网络
        MEDIUM,     // 300-800ms — 一般网络
        SLOW,       // 800-2000ms — 海外 / 弱网
        VERY_SLOW   // > 2000ms — 极慢网络
    }

    private val latencyWindow = ArrayDeque<Long>(WINDOW_SIZE)

    fun recordLatency(durationMs: Long) {
        synchronized(latencyWindow) {
            if (latencyWindow.size >= WINDOW_SIZE) {
                latencyWindow.removeFirst()
            }
            latencyWindow.addLast(durationMs)
        }
        Log.d(TAG, "latency=${durationMs}ms avg=${averageLatency}ms tier=$networkTier")
    }

    val averageLatency: Long
        get() = synchronized(latencyWindow) {
            if (latencyWindow.isEmpty()) return 500L
            latencyWindow.average().toLong()
        }

    val networkTier: NetworkTier
        get() = when {
            averageLatency < 300 -> NetworkTier.FAST
            averageLatency < 800 -> NetworkTier.MEDIUM
            averageLatency < 2000 -> NetworkTier.SLOW
            else -> NetworkTier.VERY_SLOW
        }

    /** LoadMoreLayout 提前触发加载更多的距离（距底部几个 item） */
    val preloadCount: Int
        get() = when (networkTier) {
            NetworkTier.FAST -> 2
            NetworkTier.MEDIUM -> 4
            NetworkTier.SLOW -> 6
            NetworkTier.VERY_SLOW -> 8
        }

    /** 帖子详情预取：向后预取几条帖子的详情 */
    val prefetchAhead: Int
        get() = when (networkTier) {
            NetworkTier.FAST -> 3
            NetworkTier.MEDIUM -> 5
            NetworkTier.SLOW -> 8
            NetworkTier.VERY_SLOW -> 12
        }

    /** 推荐流 / 吧内列表每页请求条数，慢网多取减少轮次 */
    val pageThreadCount: Int
        get() = when (networkTier) {
            NetworkTier.FAST -> 15
            NetworkTier.MEDIUM -> 15
            NetworkTier.SLOW -> 20
            NetworkTier.VERY_SLOW -> 25
        }
}
