package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.utils.GsonUtil

class UploadResultBean {
    @SerializedName("error_code")
    var errorCode: Int = 0

    @SerializedName("error_msg")
    var errorMsg: String? = null

    var info: UploadInfo? = null

    override fun toString(): String = GsonUtil.getGson().toJson(this)

    inner class UploadInfo {
        @SerializedName("pic_id")
        var picId: String? = null

        var width: String? = null

        var height: String? = null

        @SerializedName("pic_url")
        var picUrl: String? = null

        val pic: String
            get() = "#(pic,$picId,$width,$height)\n"
    }
}
