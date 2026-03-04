package com.huanchengfly.tieba.post.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import java.lang.reflect.Type

class ZyqDefineAdapter : JsonDeserializer<List<ForumPageBean.ZyqDefineBean>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<ForumPageBean.ZyqDefineBean> {
        val zyqDefineBeans = mutableListOf<ForumPageBean.ZyqDefineBean>()
        val jsonObject = json.asJsonObject
        for ((key, value) in jsonObject.entrySet()) {
            zyqDefineBeans.add(getZyqDefineBean(key, value.asString))
        }
        return zyqDefineBeans
    }

    private fun getZyqDefineBean(name: String, link: String): ForumPageBean.ZyqDefineBean {
        val zyqDefineBean = ForumPageBean.ZyqDefineBean()
        zyqDefineBean.name = name
        zyqDefineBean.link = link
        return zyqDefineBean
    }
}
