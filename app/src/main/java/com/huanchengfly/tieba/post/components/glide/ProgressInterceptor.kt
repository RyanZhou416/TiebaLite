package com.huanchengfly.tieba.post.components.glide

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ProgressInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val url = request.url.toString()
        val body = response.body ?: return response
        return response.newBuilder().body(ProgressResponseBody(url, body)).build()
    }

    companion object {
        @JvmField
        val LISTENER_MAP: MutableMap<String, ProgressListener> = HashMap()

        @JvmStatic
        fun addListener(url: String, listener: ProgressListener) {
            LISTENER_MAP[url] = listener
        }

        @JvmStatic
        fun removeListener(url: String) {
            LISTENER_MAP.remove(url)
        }
    }
}
