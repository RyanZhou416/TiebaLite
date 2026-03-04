package com.huanchengfly.tieba.post.utils.helios

internal class XXHashEncoder(start: Int, flag: Int) : IEncoder() {
    init {
        this.length = 32
        this.start = start
        this.flag = flag
    }

    override fun encode(bytes: ByteArray, off: Int, len: Int): EncodeResult {
        val xxHash = XXHash()
        xxHash.update(bytes, off, len)
        return EncodeResult.a(longArrayOf(xxHash.getValue()))
    }
}
