package com.huanchengfly.tieba.post.utils.helios

import java.util.zip.CRC32

internal class CRC32Encoder(start: Int, flag: Int) : IEncoder() {
    init {
        this.length = 32
        this.start = start
        this.flag = flag
    }

    override fun encode(bytes: ByteArray, off: Int, len: Int): EncodeResult {
        val result: Long = try {
            val crc32 = CRC32()
            crc32.update(bytes, off, len)
            crc32.value
        } catch (e: Exception) {
            4294967295L
        }
        return EncodeResult.a(longArrayOf(result))
    }
}
