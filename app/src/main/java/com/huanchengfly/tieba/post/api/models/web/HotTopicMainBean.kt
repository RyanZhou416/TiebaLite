package com.huanchengfly.tieba.post.api.models.web

import com.google.gson.annotations.SerializedName

class HotTopicMainBean : WebBaseBean<HotTopicMainBean.HotTopicMainDataBean>() {

    class HotTopicMainDataBean {
        @SerializedName("best_info")
        var bestInfo: BestInfoBean? = null
    }

    class BestInfoBean {
        var ret: List<BestInfoRetBean>? = null
    }

    class BestInfoRetBean {
        @SerializedName("common_type")
        var commonType: String? = null

        @SerializedName("module_name")
        var moduleName: String? = null

        @SerializedName("module_recoms")
        var moduleRecoms: List<String>? = null

        @SerializedName("thread_list")
        var threadList: Map<String, ThreadBean>? = null

        @SerializedName("recom_type")
        var recomType: String? = null

        @SerializedName("topic_id")
        var topicId: String? = null
    }

    class ThreadBean {
        @SerializedName("abstract")
        var abstracts: String? = null

        @SerializedName("agree_num")
        var agreeNum: String? = null

        var avatar: String? = null

        @SerializedName("create_time")
        var createTime: String? = null

        @SerializedName("forum_id")
        var forumId: String? = null

        @SerializedName("forum_name")
        var forumName: String? = null

        var media: List<MediaBean>? = null

        @SerializedName("name_show")
        var nameShow: String? = null

        @SerializedName("post_num")
        var postNum: String? = null

        @SerializedName("thread_id")
        var threadId: String? = null

        @SerializedName("user_id")
        var userId: String? = null

        var title: String? = null
    }

    class MediaBean {
        @SerializedName("big_pic")
        var bigPic: String? = null

        var height: Int = 0

        var width: Int = 0

        @SerializedName("small_pic")
        var smallPic: String? = null

        var type: String? = null

        @SerializedName("water_pic")
        var waterPic: String? = null
    }
}
