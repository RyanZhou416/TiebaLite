package com.huanchengfly.tieba.post.api.models.web

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

class HotMessageListBean : BaseBean() {
    @SerializedName("no")
    var errorCode: Int = 0

    @SerializedName("error")
    var errorMsg: String? = null

    var data: HotMessageListDataBean? = null

    class HotMessageListDataBean {
        var list: DataListBean? = null
    }

    class DataListBean {
        var ret: List<HotMessageRetBean>? = null
    }

    class HotMessageRetBean {
        @SerializedName("mul_id")
        var mulId: String? = null

        @SerializedName("mul_name")
        var mulName: String? = null

        @SerializedName("topic_info")
        var topicInfo: TopicInfoBean? = null
    }

    class TopicInfoBean {
        @SerializedName("topic_desc")
        var topicDesc: String? = null
    }
}
