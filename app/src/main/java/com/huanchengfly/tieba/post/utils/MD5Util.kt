package com.huanchengfly.tieba.post.utils

import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object MD5Util {
    private val yT = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    @JvmStatic
    @Throws(NoSuchAlgorithmException::class)
    fun p(paramArrayOfByte: ByteArray): String {
        val localMessageDigest = MessageDigest.getInstance("MD5")
        localMessageDigest.update(paramArrayOfByte)
        return toHexString(localMessageDigest.digest()) ?: ""
    }

    @JvmStatic
    fun toHexString(paramArrayOfByte: ByteArray?): String? {
        if (paramArrayOfByte == null) return null
        val sb = StringBuilder(paramArrayOfByte.size * 2)
        for (b in paramArrayOfByte) {
            sb.append(yT[(b.toInt() and 0xF0).ushr(4)])
            sb.append(yT[b.toInt() and 0xF])
        }
        return sb.toString()
    }

    @JvmStatic
    fun toMd5(paramString: String?): String? {
        if (paramString == null) return null
        return try {
            p(paramString.toByteArray(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            null
        }
    }

    @JvmStatic
    fun toMd5(bytes: ByteArray?): String? {
        if (bytes == null) return null
        return try {
            p(bytes)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun toMd5(file: File): String {
        if (!file.isFile) return ""
        val buffer = ByteArray(1024)
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val input = FileInputStream(file)
            var len: Int
            while (input.read(buffer, 0, 1024).also { len = it } != -1) {
                digest.update(buffer, 0, len)
            }
            input.close()
            toHexString(digest.digest()) ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
