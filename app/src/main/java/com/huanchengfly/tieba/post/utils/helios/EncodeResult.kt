package com.huanchengfly.tieba.post.utils.helios

import java.io.Serializable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.LongBuffer
import java.util.Arrays

class EncodeResult : Serializable, Cloneable {
    private var words: LongArray = LongArray(0)
    @Transient
    private var wordsInUse: Int = 0
    @Transient
    private var sizeIsSticky: Boolean = false

    constructor() {
        initWords(64)
        sizeIsSticky = false
    }

    constructor(bits: Int) {
        if (bits < 0) throw NegativeArraySizeException("nbits < 0: $bits")
        initWords(bits)
        sizeIsSticky = true
    }

    private constructor(wordsParam: LongArray) {
        words = wordsParam
        wordsInUse = wordsParam.size
        checkInvariants()
    }

    private fun checkInvariants() {
        if (!assertionsDisabled && wordsInUse != 0 && words[wordsInUse - 1] == 0L) {
            throw AssertionError()
        }
        if (!assertionsDisabled && wordsInUse > words.size) {
            throw AssertionError()
        }
        if (!assertionsDisabled && wordsInUse != words.size && words[wordsInUse] != 0L) {
            throw AssertionError()
        }
    }

    private fun recalculateWordsInUse() {
        var idx = wordsInUse - 1
        while (idx >= 0 && words[idx] == 0L) {
            idx--
        }
        wordsInUse = idx + 1
    }

    private fun trimToSize() {
        if (wordsInUse != words.size) {
            words = Arrays.copyOf(words, wordsInUse)
            checkInvariants()
        }
    }

    private fun initWords(bits: Int) {
        words = LongArray(wordIndex(bits - 1) + 1)
    }

    private fun ensureCapacity(capacity: Int) {
        if (words.size < capacity) {
            val newSize = maxOf(words.size * 2, capacity)
            words = Arrays.copyOf(words, newSize)
            sizeIsSticky = false
        }
    }

    private fun expandTo(wordIdx: Int) {
        val required = wordIdx + 1
        if (wordsInUse < required) {
            ensureCapacity(required)
            wordsInUse = required
        }
    }

    fun a(bitIndex: Int) {
        if (bitIndex < 0) throw IndexOutOfBoundsException("bitIndex < 0: $bitIndex")
        val wordIdx = wordIndex(bitIndex)
        expandTo(wordIdx)
        words[wordIdx] = words[wordIdx] xor (1L shl bitIndex)
        recalculateWordsInUse()
        checkInvariants()
    }

    fun a(fromIndex: Int, toIndex: Int) {
        checkIndex(fromIndex, toIndex)
        if (fromIndex == toIndex) return
        val startWordIdx = wordIndex(fromIndex)
        val endWordIdx = wordIndex(toIndex - 1)
        expandTo(endWordIdx)
        val firstWordMask = (-1L) shl fromIndex
        val lastWordMask = (-1L).ushr(-toIndex)
        if (startWordIdx == endWordIdx) {
            words[startWordIdx] = words[startWordIdx] xor (firstWordMask and lastWordMask)
        } else {
            words[startWordIdx] = words[startWordIdx] xor firstWordMask
            for (i in startWordIdx + 1 until endWordIdx) {
                words[i] = words[i].inv()
            }
            words[endWordIdx] = words[endWordIdx] xor lastWordMask
        }
        recalculateWordsInUse()
        checkInvariants()
    }

    fun a(fromIndex: Int, toIndex: Int, value: Boolean) {
        if (value) b(fromIndex, toIndex)
        else c(fromIndex, toIndex)
    }

    fun a(bitIndex: Int, value: Boolean) {
        if (value) b(bitIndex)
        else c(bitIndex)
    }

    fun a(other: EncodeResult): Boolean {
        var idx = minOf(wordsInUse, other.wordsInUse) - 1
        while (idx >= 0) {
            if ((words[idx] and other.words[idx]) != 0L) return true
            idx--
        }
        return false
    }

    fun a(): ByteArray {
        val n = wordsInUse
        if (n == 0) return ByteArray(0)
        var byteLen = (n - 1) * 8
        var lastWord = words[n - 1]
        while (lastWord != 0L) {
            byteLen++
            lastWord = lastWord.ushr(8)
        }
        val bytes = ByteArray(byteLen)
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until n - 1) {
            buf.putLong(words[i])
        }
        lastWord = words[n - 1]
        while (lastWord != 0L) {
            buf.put((255L and lastWord).toInt().toByte())
            lastWord = lastWord.ushr(8)
        }
        return bytes
    }

    fun b(bitIndex: Int) {
        if (bitIndex < 0) throw IndexOutOfBoundsException("bitIndex < 0: $bitIndex")
        val wordIdx = wordIndex(bitIndex)
        expandTo(wordIdx)
        words[wordIdx] = words[wordIdx] or (1L shl bitIndex)
        checkInvariants()
    }

    fun b(fromIndex: Int, toIndex: Int) {
        checkIndex(fromIndex, toIndex)
        if (fromIndex == toIndex) return
        val startWordIdx = wordIndex(fromIndex)
        val endWordIdx = wordIndex(toIndex - 1)
        expandTo(endWordIdx)
        val firstWordMask = (-1L) shl fromIndex
        val lastWordMask = (-1L).ushr(-toIndex)
        if (startWordIdx == endWordIdx) {
            words[startWordIdx] = words[startWordIdx] or (firstWordMask and lastWordMask)
        } else {
            words[startWordIdx] = words[startWordIdx] or firstWordMask
            for (i in startWordIdx + 1 until endWordIdx) {
                words[i] = -1L
            }
            words[endWordIdx] = words[endWordIdx] or lastWordMask
        }
        checkInvariants()
    }

    fun b(other: EncodeResult) {
        if (this === other) return
        while (wordsInUse > other.wordsInUse) {
            wordsInUse--
            words[wordsInUse] = 0L
        }
        for (i in 0 until wordsInUse) {
            words[i] = words[i] and other.words[i]
        }
        recalculateWordsInUse()
        checkInvariants()
    }

    fun b(): LongArray = Arrays.copyOf(words, wordsInUse)

    fun c() {
        while (wordsInUse > 0) {
            wordsInUse--
            words[wordsInUse] = 0L
        }
    }

    fun c(bitIndex: Int) {
        if (bitIndex < 0) throw IndexOutOfBoundsException("bitIndex < 0: $bitIndex")
        val wordIdx = wordIndex(bitIndex)
        if (wordIdx < wordsInUse) {
            words[wordIdx] = words[wordIdx] and (1L shl bitIndex).inv()
            recalculateWordsInUse()
            checkInvariants()
        }
    }

    fun c(fromIndex: Int, toIndex: Int) {
        var toIdx = toIndex
        checkIndex(fromIndex, toIdx)
        if (fromIndex == toIdx) return
        val startWordIdx = wordIndex(fromIndex)
        if (startWordIdx >= wordsInUse) return
        var endWordIdx = wordIndex(toIdx - 1)
        if (endWordIdx >= wordsInUse) {
            toIdx = d()
            endWordIdx = wordsInUse - 1
        }
        val firstWordMask = (-1L) shl fromIndex
        val lastWordMask = (-1L).ushr(-toIdx)
        if (startWordIdx == endWordIdx) {
            words[startWordIdx] = words[startWordIdx] and (firstWordMask and lastWordMask).inv()
        } else {
            words[startWordIdx] = words[startWordIdx] and firstWordMask.inv()
            for (i in startWordIdx + 1 until endWordIdx) {
                words[i] = 0L
            }
            words[endWordIdx] = words[endWordIdx] and lastWordMask.inv()
        }
        recalculateWordsInUse()
        checkInvariants()
    }

    fun c(other: EncodeResult) {
        if (this === other) return
        val wordsInCommon = minOf(wordsInUse, other.wordsInUse)
        if (wordsInUse < other.wordsInUse) {
            ensureCapacity(other.wordsInUse)
            wordsInUse = other.wordsInUse
        }
        for (i in 0 until wordsInCommon) {
            words[i] = words[i] or other.words[i]
        }
        if (wordsInCommon < other.wordsInUse) {
            System.arraycopy(other.words, wordsInCommon, words, wordsInCommon, wordsInUse - wordsInCommon)
        }
        checkInvariants()
    }

    public override fun clone(): Any {
        if (!sizeIsSticky) {
            trimToSize()
        }
        return try {
            val result = super.clone() as EncodeResult
            result.words = words.clone()
            result.checkInvariants()
            result
        } catch (e: CloneNotSupportedException) {
            throw InternalError()
        }
    }

    fun d(): Int {
        return if (wordsInUse == 0) {
            0
        } else {
            (wordsInUse - 1) * 64 + (64 - java.lang.Long.numberOfLeadingZeros(words[wordsInUse - 1]))
        }
    }

    fun d(fromIndex: Int, toIndex: Int): EncodeResult {
        checkIndex(fromIndex, toIndex)
        checkInvariants()
        val len = d()
        if (len <= fromIndex || fromIndex == toIndex) {
            return EncodeResult(0)
        }
        val actualTo = minOf(toIndex, len)
        val result = EncodeResult(actualTo - fromIndex)
        val targetWordCount = wordIndex(actualTo - fromIndex - 1) + 1
        var sourceWordIdx = wordIndex(fromIndex)
        val aligned = (fromIndex and 63) == 0

        var i = 0
        while (i < targetWordCount - 1) {
            result.words[i] = if (aligned) {
                words[sourceWordIdx]
            } else {
                words[sourceWordIdx].ushr(fromIndex) or (words[sourceWordIdx + 1] shl -fromIndex)
            }
            i++
            sourceWordIdx++
        }

        val lastMask = (-1L).ushr(-actualTo)
        result.words[targetWordCount - 1] = if ((actualTo - 1 and 63) < (fromIndex and 63)) {
            val lower = words[sourceWordIdx]
            (lastMask and words[sourceWordIdx + 1]) shl -fromIndex or lower.ushr(fromIndex)
        } else {
            (lastMask and words[sourceWordIdx]).ushr(fromIndex)
        }
        result.wordsInUse = targetWordCount
        result.recalculateWordsInUse()
        result.checkInvariants()
        return result
    }

    fun d(other: EncodeResult) {
        val wordsInCommon = minOf(wordsInUse, other.wordsInUse)
        if (wordsInUse < other.wordsInUse) {
            ensureCapacity(other.wordsInUse)
            wordsInUse = other.wordsInUse
        }
        for (i in 0 until wordsInCommon) {
            words[i] = words[i] xor other.words[i]
        }
        if (wordsInCommon < other.wordsInUse) {
            System.arraycopy(other.words, wordsInCommon, words, wordsInCommon, other.wordsInUse - wordsInCommon)
        }
        recalculateWordsInUse()
        checkInvariants()
    }

    fun d(bitIndex: Int): Boolean {
        if (bitIndex < 0) throw IndexOutOfBoundsException("bitIndex < 0: $bitIndex")
        checkInvariants()
        val wordIdx = wordIndex(bitIndex)
        return wordIdx < wordsInUse && (words[wordIdx] and (1L shl bitIndex)) != 0L
    }

    fun e(fromIndex: Int): Int {
        if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex < 0: $fromIndex")
        checkInvariants()
        var wordIdx = wordIndex(fromIndex)
        if (wordIdx >= wordsInUse) return -1
        var word = words[wordIdx] and ((-1L) shl fromIndex)
        while (true) {
            if (word != 0L) {
                return wordIdx * 64 + java.lang.Long.numberOfTrailingZeros(word)
            }
            wordIdx++
            if (wordIdx == wordsInUse) return -1
            word = words[wordIdx]
        }
    }

    fun e(other: EncodeResult) {
        for (i in minOf(wordsInUse, other.wordsInUse) - 1 downTo 0) {
            words[i] = words[i] and other.words[i].inv()
        }
        recalculateWordsInUse()
        checkInvariants()
    }

    fun e(): Boolean = wordsInUse == 0

    override fun equals(other: Any?): Boolean {
        if (other !is EncodeResult) return false
        if (this === other) return true
        checkInvariants()
        other.checkInvariants()
        if (wordsInUse != other.wordsInUse) return false
        for (i in 0 until wordsInUse) {
            if (words[i] != other.words[i]) return false
        }
        return true
    }

    fun f(): Int {
        var count = 0
        for (i in 0 until wordsInUse) {
            count += java.lang.Long.bitCount(words[i])
        }
        return count
    }

    fun f(fromIndex: Int): Int {
        if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex < 0: $fromIndex")
        checkInvariants()
        var wordIdx = wordIndex(fromIndex)
        if (wordIdx >= wordsInUse) return fromIndex
        var word = words[wordIdx].inv() and ((-1L) shl fromIndex)
        while (word == 0L) {
            wordIdx++
            if (wordIdx == wordsInUse) return wordsInUse * 64
            word = words[wordIdx].inv()
        }
        return wordIdx * 64 + java.lang.Long.numberOfTrailingZeros(word)
    }

    fun h(fromIndex: Int): Int {
        if (fromIndex < 0) {
            if (fromIndex != -1) throw IndexOutOfBoundsException("fromIndex < -1: $fromIndex")
            return -1
        }
        checkInvariants()
        var wordIdx = wordIndex(fromIndex)
        if (wordIdx >= wordsInUse) return fromIndex
        var word = words[wordIdx].inv() and ((-1L).ushr(-(fromIndex + 1)))
        while (word == 0L) {
            if (wordIdx == 0) return -1
            wordIdx--
            word = words[wordIdx].inv()
        }
        return (wordIdx + 1) * 64 - 1 - java.lang.Long.numberOfLeadingZeros(word)
    }

    override fun hashCode(): Int {
        var h = 1234L
        for (i in wordsInUse - 1 downTo 0) {
            h = h xor (words[i] * (i + 1).toLong())
        }
        return (h shr 32 xor h).toInt()
    }

    override fun toString(): String {
        checkInvariants()
        val numBits = if (wordsInUse > 128) f() else wordsInUse * 64
        val sb = StringBuilder(numBits * 6 + 2)
        sb.append('{')
        var i = e(0)
        if (i != -1) {
            sb.append(i)
            i = e(i + 1)
            while (i >= 0) {
                val endOfRun = f(i)
                do {
                    sb.append(", ").append(i)
                    i++
                } while (i < endOfRun)
                i = e(i + 1)
            }
        }
        sb.append('}')
        return sb.toString()
    }

    companion object {
        private val assertionsDisabled: Boolean = !EncodeResult::class.java.desiredAssertionStatus()

        private fun checkIndex(fromIndex: Int, toIndex: Int) {
            if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex < 0: $fromIndex")
            if (toIndex < 0) throw IndexOutOfBoundsException("toIndex < 0: $toIndex")
            if (fromIndex > toIndex) throw IndexOutOfBoundsException("fromIndex: $fromIndex > toIndex: $toIndex")
        }

        private fun wordIndex(bitIndex: Int): Int = bitIndex shr 6

        @JvmStatic
        fun a(buffer: ByteBuffer): EncodeResult {
            val sliced = buffer.slice().order(ByteOrder.LITTLE_ENDIAN)
            var n = sliced.remaining()
            while (n > 0 && sliced.get(n - 1).toInt() == 0) n--
            val w = LongArray((n + 7) / 8)
            sliced.limit(n)
            var wordIdx = 0
            while (sliced.remaining() >= 8) {
                w[wordIdx] = sliced.getLong()
                wordIdx++
            }
            val remaining = sliced.remaining()
            for (i in 0 until remaining) {
                w[wordIdx] = w[wordIdx] or ((sliced.get().toLong() and 255L) shl (i * 8))
            }
            return EncodeResult(w)
        }

        @JvmStatic
        fun a(buffer: LongBuffer): EncodeResult {
            val sliced = buffer.slice()
            var n = sliced.remaining()
            while (n > 0 && sliced.get(n - 1) == 0L) n--
            val w = LongArray(n)
            sliced.get(w)
            return EncodeResult(w)
        }

        @JvmStatic
        fun a(bytes: ByteArray): EncodeResult = a(ByteBuffer.wrap(bytes))

        @JvmStatic
        fun a(longs: LongArray): EncodeResult {
            var n = longs.size
            while (n > 0 && longs[n - 1] == 0L) n--
            return EncodeResult(Arrays.copyOf(longs, n))
        }
    }
}
