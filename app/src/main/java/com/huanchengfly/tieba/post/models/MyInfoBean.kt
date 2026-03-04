package com.huanchengfly.tieba.post.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.api.models.web.WebBaseBean

class MyInfoBean : WebBaseBean<MyInfoBean.MyInfoDataBean>() {
    class MyInfoDataBean {
        @SerializedName("itb_tbs")
        var itbTbs: String? = null

        var tbs: String? = null

        @SerializedName("portrait_url")
        var avatarUrl: String? = null

        var uid: Long = 0

        @SerializedName("user_sex")
        var userSex: Int = 0

        @SerializedName("name_show")
        var showName: String? = null

        var intro: String? = null

        var name: String? = null

        @SerializedName("concern_num")
        var concernNum: String? = null

        @SerializedName("fans_num")
        var fansNum: String? = null

        @SerializedName("like_forum_num")
        var likeForumNum: String? = null

        @SerializedName("post_num")
        var postNum: String? = null

        @SerializedName("is_login")
        var isLogin: Boolean = false
    }
}
