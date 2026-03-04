package com.huanchengfly.tieba.post.api.caster

abstract class ICaster<A, B> {
    abstract fun cast(a: A): B
}
