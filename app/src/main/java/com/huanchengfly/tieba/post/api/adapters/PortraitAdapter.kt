package com.huanchengfly.tieba.post.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.huanchengfly.tieba.post.utils.StringUtil
import java.lang.reflect.Type

class PortraitAdapter : JsonDeserializer<String> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): String {
        return StringUtil.getAvatarUrl(getNonNullString(json))
    }

    private fun getNonNullString(jsonElement: JsonElement?): String {
        return if (jsonElement != null && !jsonElement.isJsonNull) jsonElement.asString else ""
    }
}
