package com.huanchengfly.tieba.post.repository

import android.util.Log
import com.github.panpf.sketch.request.Depth
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.enqueue
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.post.ui.common.PicContentRender
import java.util.concurrent.ConcurrentHashMap

/**
 * 帖子内图片预加载管理器。
 *
 * 根据当前可见的帖子位置，提前预加载后续帖子中的图片到磁盘缓存，
 * 使用户滚动时图片能更快展示。
 */
object ImagePrefetchManager {

    private const val TAG = "ImagePrefetch"

    private val prefetchedUrls = ConcurrentHashMap.newKeySet<String>()

    private val prefetchAhead: Int
        get() = when (AdaptivePrefetchManager.networkTier) {
            AdaptivePrefetchManager.NetworkTier.FAST -> 3
            AdaptivePrefetchManager.NetworkTier.MEDIUM -> 5
            AdaptivePrefetchManager.NetworkTier.SLOW -> 8
            AdaptivePrefetchManager.NetworkTier.VERY_SLOW -> 12
        }

    fun prefetchImagesForPosts(
        allRenders: List<List<PbContentRender>>,
        currentVisibleEnd: Int,
    ) {
        val ahead = prefetchAhead
        val start = (currentVisibleEnd + 1).coerceAtMost(allRenders.size)
        val end = (currentVisibleEnd + 1 + ahead).coerceAtMost(allRenders.size)
        if (start >= end) return

        val context = App.INSTANCE
        for (i in start until end) {
            val renders = allRenders[i]
            for (render in renders) {
                if (render !is PicContentRender) continue
                val url = render.picUrl
                if (url.isEmpty() || !prefetchedUrls.add(url)) continue

                ImageRequest(context, url) {
                    depth(Depth.NETWORK)
                }.enqueue()

                if (render.thumbnailUrl.isNotEmpty() && render.thumbnailUrl != url) {
                    val thumbUrl = render.thumbnailUrl
                    if (prefetchedUrls.add(thumbUrl)) {
                        ImageRequest(context, thumbUrl) {
                            depth(Depth.NETWORK)
                        }.enqueue()
                    }
                }
            }
        }
        Log.d(TAG, "Prefetched images for posts $start..<$end (ahead=$ahead)")
    }

    fun clear() {
        prefetchedUrls.clear()
    }
}
