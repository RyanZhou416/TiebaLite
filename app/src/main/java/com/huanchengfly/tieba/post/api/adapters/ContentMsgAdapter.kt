package com.huanchengfly.tieba.post.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.huanchengfly.tieba.post.api.models.ThreadContentBean
import java.lang.reflect.Type

class ContentMsgAdapter : JsonDeserializer<List<ThreadContentBean.ContentBean>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<ThreadContentBean.ContentBean> {
        val list = mutableListOf<ThreadContentBean.ContentBean>()
        if (json.isJsonArray) {
            for (element in json.asJsonArray) {
                if (element.isJsonObject) {
                    list.add(context.deserialize(element, ThreadContentBean.ContentBean::class.java))
                }
            }
        }
        return list
    }
}
