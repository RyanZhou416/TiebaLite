package com.huanchengfly.tieba.post.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.huanchengfly.tieba.post.api.models.UserPostBean
import java.lang.reflect.Type

class UserPostContentAdapter : JsonDeserializer<List<UserPostBean.ContentBean>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<UserPostBean.ContentBean> {
        val list = mutableListOf<UserPostBean.ContentBean>()
        if (json.isJsonArray) {
            for (jsonElement in json.asJsonArray) {
                if (jsonElement.isJsonObject) {
                    val jsonObject = jsonElement.asJsonObject
                    val contentBean = UserPostBean.ContentBean()
                        .setCreateTime(getString(jsonObject.get("create_time")))
                        .setPostId(getString(jsonObject.get("post_id")))
                    val postContentBeans = mutableListOf<UserPostBean.PostContentBean>()
                    val contentElement = jsonObject.get("post_content")
                    if (contentElement?.isJsonArray == true) {
                        for (element in contentElement.asJsonArray) {
                            if (element.isJsonObject) {
                                val postContentObject = element.asJsonObject
                                postContentBeans.add(
                                    UserPostBean.PostContentBean()
                                        .setType(getNonNullString(postContentObject.get("type"), "0"))
                                        .setText(getNonNullString(postContentObject.get("text"), ""))
                                )
                            }
                        }
                    }
                    contentBean.setPostContent(postContentBeans)
                    list.add(contentBean)
                }
            }
        }
        return list
    }

    private fun getString(jsonElement: JsonElement?): String? {
        return if (jsonElement != null && !jsonElement.isJsonNull) jsonElement.asString else null
    }

    private fun getNonNullString(jsonElement: JsonElement?, defValue: String): String {
        return if (jsonElement != null && !jsonElement.isJsonNull) jsonElement.asString else defValue
    }
}
