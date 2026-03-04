package com.huanchengfly.tieba.post.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.huanchengfly.tieba.post.api.models.SearchUserBean
import java.lang.reflect.Type

class UserFuzzyMatchAdapter : JsonDeserializer<List<SearchUserBean.UserBean>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<SearchUserBean.UserBean> {
        val userBeans = mutableListOf<SearchUserBean.UserBean>()
        when {
            json.isJsonArray -> {
                for (element in json.asJsonArray) {
                    if (element.isJsonObject) {
                        userBeans.add(getUserBean(element.asJsonObject))
                    }
                }
            }
            json.isJsonObject -> {
                for ((_, value) in json.asJsonObject.entrySet()) {
                    if (value.isJsonObject) {
                        userBeans.add(getUserBean(value.asJsonObject))
                    }
                }
            }
        }
        return userBeans
    }

    private fun getNonNullString(jsonElement: JsonElement): String? {
        return if (jsonElement.isJsonNull) null else jsonElement.asString
    }

    private fun getUserBean(jsonObject: JsonObject): SearchUserBean.UserBean {
        return SearchUserBean.UserBean(
            getNonNullString(jsonObject.get("id")),
            getNonNullString(jsonObject.get("intro")),
            getNonNullString(jsonObject.get("user_nickname")),
            getNonNullString(jsonObject.get("show_nickname")),
            getNonNullString(jsonObject.get("name")),
            getNonNullString(jsonObject.get("portrait")),
            getNonNullString(jsonObject.get("fans_num")),
            jsonObject.get("has_concerned").asInt
        )
    }
}
