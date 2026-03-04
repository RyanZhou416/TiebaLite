package com.huanchengfly.tieba.post.utils.helios

internal open class XXHash(private val seed: Int = 0) {
    private val state = IntArray(4)
    private val buffer = ByteArray(16)
    private var totalLength = 0
    private var bufferPos = 0

    init {
        reset()
    }

    private fun reset() {
        state[0] = seed - 1640531535 - 2048144777
        state[1] = seed - 2048144777
        state[2] = seed
        state[3] = seed + 1640531535
    }

    private fun process16Bytes(data: ByteArray, offset: Int) {
        var v0 = state[0]
        var v1 = state[1]
        var v2 = state[2]
        var v3 = state[3]
        v0 = Integer.rotateLeft(v0 + readInt(data, offset) * -2048144777, 13)
        v1 = Integer.rotateLeft(v1 + readInt(data, offset + 4) * -2048144777, 13)
        v2 = Integer.rotateLeft(v2 + readInt(data, offset + 8) * -2048144777, 13)
        v3 = Integer.rotateLeft(v3 + readInt(data, offset + 12) * -2048144777, 13)
        state[0] = v0 * -1640531535
        state[1] = v1 * -1640531535
        state[2] = v2 * -1640531535
        state[3] = v3 * -1640531535
        bufferPos = 0
    }

    internal fun update(data: ByteArray, off: Int, len: Int) {
        if (len <= 0) return
        totalLength += len
        val end = off + len
        if (bufferPos + len < 16) {
            System.arraycopy(data, off, buffer, bufferPos, len)
            bufferPos += len
        } else {
            var pos = off
            if (bufferPos > 0) {
                val remaining = 16 - bufferPos
                System.arraycopy(data, off, buffer, bufferPos, remaining)
                process16Bytes(buffer, 0)
                pos = off + remaining
            }

            while (pos <= end - 16) {
                process16Bytes(data, pos)
                pos += 16
            }

            if (pos < end) {
                bufferPos = end - pos
                System.arraycopy(data, pos, buffer, 0, bufferPos)
            }
        }
    }

    internal fun getValue(): Long {
        var hash: Int = if (totalLength > 16) {
            Integer.rotateLeft(state[0], 1) +
                    Integer.rotateLeft(state[1], 7) +
                    Integer.rotateLeft(state[2], 12) +
                    Integer.rotateLeft(state[3], 18)
        } else {
            state[2] + 374761393
        }

        hash += totalLength
        val remaining = bufferPos

        var i = 0
        while (i <= remaining - 4) {
            hash = Integer.rotateLeft(hash + readInt(buffer, i) * -1028477379, 17) * 668265263
            i += 4
        }

        while (i < bufferPos) {
            hash = Integer.rotateLeft((buffer[i].toInt() and 255) * 374761393 + hash, 11) * -1640531535
            i++
        }

        hash = (hash.ushr(15) xor hash) * -2048144777
        hash = (hash xor hash.ushr(13)) * -1028477379
        return (hash xor hash.ushr(16)).toLong() and 4294967295L
    }

    companion object {
        private fun readInt(data: ByteArray, offset: Int): Int =
            (readLong(data, offset) and 4294967295L).toInt()

        private fun readLong(data: ByteArray, offset: Int): Long {
            var result = 0L
            for (i in 0 until 4) {
                result = result or ((data[offset + i].toLong() and 255L) shl (i * 8))
            }
            return result
        }
    }
}
