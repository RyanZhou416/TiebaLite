package com.huanchengfly.tieba.post.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import java.lang.reflect.Type

class MediaAdapter : JsonDeserializer<List<ForumPageBean.MediaInfoBean>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<ForumPageBean.MediaInfoBean> {
        val mediaInfoBeans = mutableListOf<ForumPageBean.MediaInfoBean>()
        if (json.isJsonArray) {
            for (element in json.asJsonArray) {
                if (element.isJsonObject) {
                    val jsonObject = element.asJsonObject
                    val type = getNonNullString(jsonObject.get("type"))
                    if (type == "3") {
                        mediaInfoBeans.add(
                            ForumPageBean.MediaInfoBean()
                                .setType(type)
                                .setBigPic(getNonNullString(jsonObject.get("big_pic")))
                                .setOriginPic(getNonNullString(jsonObject.get("origin_pic")))
                                .setSrcPic(getNonNullString(jsonObject.get("src_pic")))
                                .setPostId(getNonNullString(jsonObject.get("post_id")))
                                .setIsLongPic(getNonNullString(jsonObject.get("is_long_pic")))
                                .setShowOriginalBtn(getNonNullString(jsonObject.get("show_original_btn")))
                        )
                    }
                }
            }
        }
        return mediaInfoBeans
    }

    private fun getNonNullString(jsonElement: JsonElement?): String? {
        return if (jsonElement != null && !jsonElement.isJsonNull) jsonElement.asString else null
    }
}
