package com.huanchengfly.tieba.post.utils.helios

internal class HashResult {
    private val mEncodeResult: EncodeResult = EncodeResult(BITS)

    init {
        mEncodeResult.a(0, BITS, true)
    }

    fun update(encodedValue: EncodeResult, start: Int, length: Int, flag: Int) {
        val subResult = mEncodeResult.d(start, start + length)
        when (flag) {
            0 -> subResult.b(encodedValue)
            2 -> subResult.e(encodedValue)
            3 -> subResult.c(encodedValue)
            1 -> subResult.d(encodedValue)
            else -> subResult.d(encodedValue)
        }
        for (i in 0 until length) {
            mEncodeResult.a(start + i, subResult.d(i))
        }
    }

    fun getValue(): ByteArray = mEncodeResult.a()

    companion object {
        @JvmField
        var BITS = 40
    }
}
