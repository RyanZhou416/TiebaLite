package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

object AssetUtil {
    @JvmField
    var TYPE_CSS = "text/css"
    @JvmField
    var TYPE_JS = "application/javascript"
    @JvmField
    var TYPE_FONT_WOFF = "application/x-font-woff"

    @JvmStatic
    fun getResponseFromAssets(context: Context, filename: String, mimeType: String): WebResourceResponse? {
        if (filename.isEmpty()) return null
        val inputStream = try {
            context.assets.open(filename)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return WebResourceResponse(mimeType, "utf-8", inputStream)
    }

    @JvmStatic
    fun getEmptyResponse(): WebResourceResponse =
        WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))

    @JvmStatic
    fun getStringFromAsset(context: Context?, file: String): String {
        if (context == null) return ""
        return try {
            val inputStream = context.assets.open(file)
            val length = inputStream.available()
            val buffer = ByteArray(length)
            inputStream.read(buffer)
            String(buffer, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }
}
