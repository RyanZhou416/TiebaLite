package com.huanchengfly.tieba.post.components.glide

import android.util.Log
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.io.IOException

class ProgressResponseBody(
    url: String,
    private val responseBody: ResponseBody
) : ResponseBody() {

    private var bufferedSource: BufferedSource? = null
    private var listener: ProgressListener? = ProgressInterceptor.LISTENER_MAP[url]

    override fun contentType(): MediaType? = responseBody.contentType()

    override fun contentLength(): Long = responseBody.contentLength()

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = ProgressSource(responseBody.source()).buffer()
        }
        return bufferedSource ?: throw IllegalStateException("BufferedSource should not be null")
    }

    private inner class ProgressSource(source: Source) : ForwardingSource(source) {
        var totalBytesRead: Long = 0
        var currentProgress: Int = 0

        @Throws(IOException::class)
        override fun read(sink: Buffer, byteCount: Long): Long {
            val bytesRead = super.read(sink, byteCount)
            val fullLength = responseBody.contentLength()
            if (bytesRead == -1L) {
                totalBytesRead = fullLength
            } else {
                totalBytesRead += bytesRead
            }
            val progress = (100f * totalBytesRead / fullLength).toInt()
            Log.d(TAG, "download progress is $progress")
            if (listener != null && progress != currentProgress) {
                listener?.onProgress(progress)
            }
            if (listener != null && totalBytesRead == fullLength) {
                listener = null
            }
            currentProgress = progress
            return bytesRead
        }
    }

    companion object {
        private const val TAG = "XGlide"
    }
}
