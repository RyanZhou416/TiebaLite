package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName

class WebReplyResultBean {
    @SerializedName("no")
    var errorCode: Int = 0

    @SerializedName("error")
    var errorMsg: String? = null

    var data: WebReplyDataBean? = null

    class WebReplyDataBean {
        @SerializedName("is_not_top_stick")
        var isNotTopStick: Int = 0

        var pid: Long = 0

        var tid: Long = 0
    }
}
