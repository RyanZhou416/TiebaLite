package com.huanchengfly.tieba.post.utils.helios

import java.util.Arrays

object Hasher {
    private val sEncoders: Array<IEncoder> = arrayOf(
        CRC32Encoder(8, 0),
        XXHashEncoder(0, 1),
        XXHashEncoder(1, 1),
        CRC32Encoder(7, 1)
    )

    @JvmStatic
    fun hash(bytes: ByteArray): ByteArray {
        val result = HashResult()
        val newBytes = ByteArrayUtils.copyArray(bytes, bytes.size + (sEncoders.size + 1) * 5)
        ByteArrayUtils.copyArray(newBytes, result.getValue(), bytes.size)

        for (i in sEncoders.indices) {
            val encoder = sEncoders[i]
            val len = bytes.size + (i + 1) * 5
            result.update(encoder.encode(newBytes, 0, len), encoder.start, encoder.length, encoder.flag)
            ByteArrayUtils.copyArray(newBytes, result.getValue(), len)
        }

        return Arrays.copyOf(result.getValue(), 5)
    }
}
