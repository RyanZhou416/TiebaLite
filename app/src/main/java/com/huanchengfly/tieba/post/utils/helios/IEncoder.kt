package com.huanchengfly.tieba.post.utils.helios

abstract class IEncoder {
    var length: Int = 32
        protected set
    var start: Int = 0
        protected set
    var flag: Int = 0
        protected set

    abstract fun encode(bytes: ByteArray, off: Int, len: Int): EncodeResult
}
