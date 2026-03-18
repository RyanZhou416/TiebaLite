package com.huanchengfly.tieba.post.api.retrofit.interceptors

import com.huanchengfly.tieba.post.BuildConfig
import com.huanchengfly.tieba.post.repository.AdaptivePrefetchManager
import com.huanchengfly.tieba.post.utils.PerformanceTracker
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 记录每次 API 请求的响应延迟，供 [AdaptivePrefetchManager] 做自适应预加载决策，
 * 同时在 debug 模式下将详细数据记录到 [PerformanceTracker]。
 */
object LatencyTrackingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val startNs = System.nanoTime()
        val response = chain.proceed(chain.request())
        val durationMs = (System.nanoTime() - startNs) / 1_000_000
        AdaptivePrefetchManager.recordLatency(durationMs)
        if (BuildConfig.DEBUG) {
            PerformanceTracker.recordNetworkLatency(
                chain.request().url.toString(),
                durationMs
            )
        }
        return response
    }
}
