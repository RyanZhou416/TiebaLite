package com.huanchengfly.tieba.post.api.models.web

import com.google.gson.annotations.SerializedName

class HotTopicThreadBean : WebBaseBean<HotTopicThreadBean.HotTopicThreadDataBean>() {

    class HotTopicThreadDataBean {
        @SerializedName("thread_list")
        var threadList: List<HotTopicMainBean.ThreadBean>? = null
    }
}
