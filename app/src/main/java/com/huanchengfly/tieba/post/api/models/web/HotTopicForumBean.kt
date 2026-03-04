package com.huanchengfly.tieba.post.api.models.web

import com.google.gson.annotations.SerializedName

class HotTopicForumBean : WebBaseBean<HotTopicForumBean.HotTopicForumDataBean>() {

    class HotTopicForumDataBean {
        @SerializedName("forum_list")
        var forumList: ForumListBean? = null

        @SerializedName("pk_info")
        var pkInfo: PkInfoBean? = null
    }

    class ForumListBean {
        var output: List<ForumItemBean>? = null
    }

    class PkInfoBean {
        var ret: List<PkInfoRetBean>? = null
    }

    class PkInfoRetBean {
        @SerializedName("create_time")
        var createTime: String? = null

        @SerializedName("module_name")
        var moduleName: String? = null

        @SerializedName("module_type")
        var moduleType: String? = null

        var pics: PkPicBean? = null

        var picUrls: PkPicBean? = null

        @SerializedName("has_selected")
        var hasSelected: Boolean = false

        @SerializedName("num_coefficient")
        var numCoefficient: String? = null

        @SerializedName("pk_desc_1")
        var pkDesc1: String? = null

        @SerializedName("pk_desc_2")
        var pkDesc2: String? = null

        @SerializedName("pk_desc_3")
        var pkDesc3: String? = null

        @SerializedName("pk_desc_4")
        var pkDesc4: String? = null

        @SerializedName("pk_id")
        var pkId: String? = null

        @SerializedName("pk_num_1")
        var pkNum1: String? = null

        @SerializedName("pk_num_2")
        var pkNum2: String? = null

        @SerializedName("pk_num_3")
        var pkNum3: String? = null

        @SerializedName("pk_num_4")
        var pkNum4: String? = null

        @SerializedName("selected_index")
        var selectedIndex: String? = null

        var title: String? = null

        @SerializedName("topic_id")
        var topicId: String? = null
    }

    class ForumItemBean

    class PkPicBean {
        @SerializedName("pk_icon_1")
        var pkIcon1: String? = null

        @SerializedName("pk_icon_2")
        var pkIcon2: String? = null

        @SerializedName("pk_icon_after_1")
        var pkIconAfter1: String? = null

        @SerializedName("pk_icon_after_2")
        var pkIconAfter2: String? = null
    }
}
