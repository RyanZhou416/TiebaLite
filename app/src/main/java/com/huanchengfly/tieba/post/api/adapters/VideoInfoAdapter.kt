package com.huanchengfly.tieba.post.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import java.lang.reflect.Type

class VideoInfoAdapter : JsonDeserializer<ForumPageBean.VideoInfoBean> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ForumPageBean.VideoInfoBean? {
        if (json.isJsonArray) {
            return null
        }
        return context.deserialize(json, typeOfT)
    }
}
