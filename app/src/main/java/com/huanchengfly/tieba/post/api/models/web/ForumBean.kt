package com.huanchengfly.tieba.post.api.models.web

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.api.adapters.PortraitAdapter
import com.huanchengfly.tieba.post.api.adapters.VideoInfoAdapter
import com.huanchengfly.tieba.post.api.adapters.ZyqDefineAdapter
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import com.huanchengfly.tieba.post.models.BaseBean

class ForumBean : WebBaseBean<ForumBean.ForumDataBean>() {

    class ForumDataBean {
        @SerializedName("frs_data")
        var frsData: FrsDataBean? = null
    }

    class TaskInfoBean

    class FrsThreadBean : BaseBean() {
        var id: String? = null

        var tid: String? = null

        var title: String? = null

        @SerializedName("reply_num")
        var replyNum: String? = null

        @SerializedName("task_info")
        var taskInfo: TaskInfoBean? = null

        @SerializedName("view_num")
        var viewNum: String? = null

        @SerializedName("last_time")
        var lastTime: String? = null

        @SerializedName("last_time_int")
        var lastTimeInt: String? = null

        @SerializedName("create_time")
        var createTime: String? = null

        @SerializedName("is_top")
        var isTop: String? = null

        @SerializedName("is_good")
        var isGood: String? = null

        @SerializedName("is_livepost")
        var isLivePost: String? = null

        @SerializedName("is_ntitle")
        var isNoTitle: String? = null

        @SerializedName(value = "author", alternate = ["user"])
        var author: ForumPageBean.UserBean? = null

        @JsonAdapter(VideoInfoAdapter::class)
        @SerializedName("video_info")
        var videoInfo: ForumPageBean.VideoInfoBean? = null

        var media: List<MediaBean>? = null

        @SerializedName("abstract")
        var abstracts: String? = null

        var agree: AgreeBean? = null

        class AgreeBean {
            @SerializedName("agree_num")
            var agreeNum: Int = 0
        }
    }

    class MediaBean {
        @SerializedName("big_pic")
        var bigPic: String? = null

        @SerializedName("is_gif")
        var isGif: Boolean = false

        var height: Int = 0

        var width: Int = 0

        @SerializedName("small_pic")
        var smallPic: String? = null

        @SerializedName("static_img")
        var staticImg: String? = null

        var type: String? = null

        @SerializedName("water_pic")
        var waterPic: String? = null
    }

    class FrsDataBean {
        var anti: ForumPageBean.AntiBean? = null

        var forum: FrsForumBean? = null

        var user: FrsUserBean? = null

        var page: FrsPageBean? = null

        @SerializedName("thread_list")
        var threadList: List<FrsThreadBean>? = null
    }

    class ForumAttrBean {
        @SerializedName("zyqtitle")
        var zyqTitle: String? = null

        @JsonAdapter(ZyqDefineAdapter::class)
        @SerializedName("zyqdefine")
        var zyqDefine: List<ForumPageBean.ZyqDefineBean>? = null

        @SerializedName("zyqfriend")
        var zyqFriend: List<String>? = null
    }

    class FrsForumBean {
        var id: String? = null

        var attrs: ForumAttrBean? = null

        var name: String? = null

        @SerializedName("is_like")
        var isLike: String? = null

        @SerializedName("user_level")
        var userLevel: String? = null

        @SerializedName("level_id")
        var levelId: String? = null

        @SerializedName("level_name")
        var levelName: String? = null

        @SerializedName("is_exists")
        var isExists: Boolean = false

        @SerializedName("cur_score")
        var curScore: String? = null

        @SerializedName("levelup_score")
        var levelUpScore: String? = null

        @SerializedName("member_num")
        var memberNum: String? = null

        @SerializedName("post_num")
        var postNum: String? = null

        @SerializedName("thread_num")
        var threadNum: String? = null

        var managers: List<ForumPageBean.ManagerBean>? = null

        @SerializedName("good_classify")
        var goodClassify: List<ForumPageBean.GoodClassifyBean>? = null

        var slogan: String? = null

        var avatar: String? = null

        var tids: String? = null

        @SerializedName("sign_in_info")
        var signInInfo: ForumPageBean.ForumBean.SignInInfo? = null
    }

    class FrsUserBean {
        var id: String? = null

        var name: String? = null

        @SerializedName(value = "name_show", alternate = ["nick"])
        var nameShow: String? = null

        @JsonAdapter(PortraitAdapter::class)
        var portrait: String? = null

        @SerializedName("new_user_info")
        var newUserInfo: NewUserInfoBean? = null
    }

    class NewUserInfoBean {
        @SerializedName("user_id")
        var userId: String? = null

        @SerializedName("user_name")
        var userName: String? = null

        @SerializedName("user_nickname")
        var userNickname: String? = null

        @SerializedName("user_sex")
        var userSex: Int = 0
    }

    class FrsPageBean {
        @SerializedName("page_size")
        var pageSize: Int = 0

        var offset: Int = 0

        @SerializedName("current_page")
        var currentPage: Int = 0

        @SerializedName("total_count")
        var totalCount: Int = 0

        @SerializedName("total_page")
        var totalPage: Int = 0

        @SerializedName("cur_good_id")
        var curGoodId: Int = 0
    }
}
