package com.huanchengfly.tieba.post.utils.helios

import java.util.Arrays

internal object ByteArrayUtils {
    fun copyArray(dest: ByteArray, src: ByteArray, destPos: Int) {
        require(destPos >= 0) { "start should be more than zero!" }
        require(dest.isNotEmpty()) { "dst array should not be null or empty" }
        require(src.isNotEmpty()) { "src array should not be null or empty" }
        require(dest.size >= src.size) { "dst array length should be longer than:${src.size}" }
        require(dest.size >= src.size + destPos) { "start should be less than:${dest.size - src.size}" }
        System.arraycopy(src, 0, dest, destPos, src.size)
    }

    fun copyArray(src: ByteArray, newLength: Int): ByteArray {
        require(src.isNotEmpty()) { "original array should not be null or empty" }
        require(newLength >= 0) { "length should be more than zero!" }
        return Arrays.copyOf(src, newLength)
    }
}
