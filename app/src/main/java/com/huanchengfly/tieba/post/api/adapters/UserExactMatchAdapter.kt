package com.huanchengfly.tieba.post.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.huanchengfly.tieba.post.api.models.SearchUserBean
import java.lang.reflect.Type

class UserExactMatchAdapter : JsonDeserializer<SearchUserBean.UserBean> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SearchUserBean.UserBean? {
        if (json.isJsonArray) {
            return null
        }
        return context.deserialize(json, typeOfT)
    }
}
