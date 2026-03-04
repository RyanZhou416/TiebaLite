package com.huanchengfly.tieba.post.api.models.web

import com.google.gson.annotations.SerializedName

class HotTopicBean : WebBaseBean<HotTopicBean.HotTopicDataBean>() {

    class HotTopicDataBean {
        @SerializedName("pmy_topic_ext")
        var pmyTopicExt: String? = null

        @SerializedName("yuren_rand")
        var yurenRand: Int = 0

        @SerializedName("topic_info")
        var topicInfo: TopicInfoBean? = null
    }

    class TopicInfoBean {
        var ret: List<TopicInfoRetBean>? = null
    }

    class TopicInfoRetBean {
        @SerializedName("create_time")
        var createTime: String? = null

        @SerializedName("discuss_num")
        var discussNum: String? = null

        @SerializedName("hot_value")
        var hotValue: String? = null

        @SerializedName("topic_id")
        var topicId: String? = null

        @SerializedName("topic_name")
        var topicName: String? = null

        @SerializedName("topic_desc")
        var topicDesc: String? = null

        var tids: String? = null

        @SerializedName("real_discuss_num")
        var realDiscussNum: String? = null

        var extra: TopicInfoRetExtraBean? = null
    }

    class TopicInfoRetExtraBean {
        @SerializedName("head_pic")
        var headPic: String? = null

        @SerializedName("share_title")
        var shareTitle: String? = null

        @SerializedName("share_pic")
        var sharePic: String? = null

        @SerializedName("topic_tid")
        var topicTid: String? = null
    }
}
