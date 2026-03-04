package com.huanchengfly.tieba.post.utils

import com.google.gson.Gson

object GsonUtil {
    @Volatile
    private var gson: Gson? = null

    @JvmStatic
    fun getGson(): Gson {
        return gson ?: synchronized(GsonUtil::class.java) {
            gson ?: Gson().also { gson = it }
        }
    }
}
