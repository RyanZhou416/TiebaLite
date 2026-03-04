package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.util.Base64
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

object CacheUtil {
    @JvmStatic
    fun <T> getCache(context: Context, cacheId: String, tClass: Class<T>): T? {
        val cacheDir = context.externalCacheDir
        val hash = MD5Util.toMd5(tClass.name + "_" + cacheId) ?: return null
        val cacheFile = File(cacheDir, hash)
        if (cacheFile.exists()) {
            try {
                return GsonUtil.getGson().fromJson(base64Decode(FileUtil.readFile(cacheFile) ?: return null), tClass)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
        }
        return null
    }

    @JvmStatic
    fun putCache(context: Context, cacheId: String, obj: Any) {
        val cacheDir = context.externalCacheDir
        val hash = MD5Util.toMd5(obj.javaClass.name + "_" + cacheId) ?: return
        val cacheFile = File(cacheDir, hash)
        try {
            if (cacheFile.exists() || cacheFile.createNewFile()) {
                try {
                    FileUtil.writeFile(cacheFile, base64Encode(GsonUtil.getGson().toJson(obj)), false)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun base64Encode(s: String): String =
        Base64.encodeToString(s.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT)

    @JvmStatic
    fun base64Decode(s: String): String =
        String(Base64.decode(s.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT), StandardCharsets.UTF_8)
}
