package com.huanchengfly.tieba.post.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.huanchengfly.tieba.post.api.models.ThreadContentBean
import java.lang.reflect.Type

class SubPostListAdapter : JsonDeserializer<ThreadContentBean.SubPostListBean> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ThreadContentBean.SubPostListBean {
        if (json.isJsonArray) {
            return ThreadContentBean.SubPostListBean()
        }
        return context.deserialize(json, typeOfT)
    }
}
