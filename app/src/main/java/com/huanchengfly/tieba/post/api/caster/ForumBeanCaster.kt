package com.huanchengfly.tieba.post.api.caster

import android.webkit.URLUtil
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import com.huanchengfly.tieba.post.api.models.web.ForumBean

class ForumBeanCaster : ICaster<ForumBean, ForumPageBean>() {
    override fun cast(a: ForumBean): ForumPageBean {
        val forumPageBean = ForumPageBean()
        val frsDataBean = a.data!!.frsData!!
        forumPageBean.errorCode = a.errorCode.toString()
        forumPageBean.errorMsg = a.errorMsg
        forumPageBean.setAnti(frsDataBean.anti)
        forumPageBean.page = FrsPageBeanImpl().cast(frsDataBean.page!!)
        forumPageBean.setForum(FrsForumBeanImpl().cast(frsDataBean.forum!!))
        forumPageBean.user = FrsUserBeanImpl().cast(frsDataBean.user!!)
        val threadBeans = mutableListOf<ForumPageBean.ThreadBean>()
        val userBeans = mutableListOf<ForumPageBean.UserBean>()
        val frsThreadBeanImpl = FrsThreadBeanImpl()
        for (frsThreadBean in frsDataBean.threadList ?: emptyList()) {
            if (frsThreadBean.lastTimeInt != null) {
                threadBeans.add(frsThreadBeanImpl.cast(frsThreadBean))
                userBeans.add(frsThreadBean.author!!)
            }
        }
        forumPageBean.threadList = threadBeans
        forumPageBean.userList = userBeans
        return forumPageBean
    }

    private class FrsThreadBeanImpl : ICaster<ForumBean.FrsThreadBean, ForumPageBean.ThreadBean>() {
        override fun cast(a: ForumBean.FrsThreadBean): ForumPageBean.ThreadBean {
            val threadBean = ForumPageBean.ThreadBean()
            val abstractBeans = mutableListOf<ForumPageBean.AbstractBean>()
            val mediaInfoBeans = mutableListOf<ForumPageBean.MediaInfoBean>()
            val mediaBeanImpl = MediaBeanImpl()
            for (mediaBean in a.media ?: emptyList()) {
                val mediaInfoBean = mediaBeanImpl.cast(mediaBean)
                if (mediaInfoBean != null) mediaInfoBeans.add(mediaInfoBean)
            }
            abstractBeans.add(ForumPageBean.AbstractBean("0", a.abstracts ?: ""))
            val agreeNum = a.agree?.agreeNum ?: 0
            threadBean.setAbstractString(a.abstracts)
                .setAbstractBeans(abstractBeans)
                .setAgreeNum(agreeNum.toString())
                .setAuthorId(a.author?.id)
                .setId(a.id)
                .setIsGood(a.isGood)
                .setIsNoTitle(a.isNoTitle)
                .setIsTop(a.isTop)
                .setLastTime(a.lastTime)
                .setLastTimeInt(a.lastTimeInt)
                .setReplyNum(a.replyNum)
                .setTid(a.tid)
                .setTitle(a.title)
                .setVideoInfo(a.videoInfo?.apply { setVideoUrl(originVideoUrl) })
                .setMedia(mediaInfoBeans)
                .setViewNum(a.viewNum)
            return threadBean
        }
    }

    private class FrsPageBeanImpl : ICaster<ForumBean.FrsPageBean, ForumPageBean.PageBean>() {
        override fun cast(a: ForumBean.FrsPageBean): ForumPageBean.PageBean {
            val pageBean = ForumPageBean.PageBean()
            pageBean.curGoodId = a.curGoodId.toString()
            pageBean.currentPage = a.currentPage.toString()
            pageBean.hasMore = if (a.currentPage < a.totalPage) "1" else "0"
            pageBean.hasPrev = if (a.currentPage > 1) "1" else "0"
            pageBean.offset = a.offset.toString()
            pageBean.pageSize = a.pageSize.toString()
            pageBean.totalCount = a.totalCount.toString()
            pageBean.totalPage = a.totalPage.toString()
            return pageBean
        }
    }

    private class FrsForumBeanImpl : ICaster<ForumBean.FrsForumBean, ForumPageBean.ForumBean>() {
        override fun cast(a: ForumBean.FrsForumBean): ForumPageBean.ForumBean {
            return ForumPageBean.ForumBean(
                a.id,
                a.name,
                a.isLike,
                a.userLevel,
                a.levelId,
                a.levelName,
                if (a.isExists) "1" else "0",
                a.curScore,
                a.levelUpScore,
                a.memberNum,
                a.threadNum,
                null,
                a.postNum,
                a.managers,
                a.attrs?.zyqTitle,
                a.attrs?.zyqDefine,
                a.attrs?.zyqFriend,
                a.goodClassify,
                a.slogan,
                a.avatar,
                a.tids,
                a.signInInfo
            )
        }
    }

    private class FrsUserBeanImpl : ICaster<ForumBean.FrsUserBean, ForumPageBean.UserBean>() {
        override fun cast(a: ForumBean.FrsUserBean): ForumPageBean.UserBean {
            val userBean = ForumPageBean.UserBean()
            userBean.id = a.id
            val newUserInfo = a.newUserInfo
            if (newUserInfo == null) {
                userBean.name = a.name
                userBean.nameShow = a.nameShow
            } else {
                userBean.name = newUserInfo.userName
                userBean.nameShow = newUserInfo.userNickname
            }
            userBean.portrait = a.portrait
            return userBean
        }
    }

    private class MediaBeanImpl : ICaster<ForumBean.MediaBean, ForumPageBean.MediaInfoBean?>() {
        override fun cast(a: ForumBean.MediaBean): ForumPageBean.MediaInfoBean? {
            if ("pic" != a.type) {
                return null
            }
            val bigPic = a.bigPic ?: return null
            val mediaInfoBean = ForumPageBean.MediaInfoBean()
            val fileName = URLUtil.guessFileName(bigPic, null, "image/jpeg")
            val origin = if (bigPic.contains(".hiphotos.baidu.com") || bigPic.contains("imgsrc.baidu.com")) {
                "http://imgsrc.baidu.com/forum/pic/item/$fileName"
            } else {
                "http://imgsa.baidu.com/forum/pic/item/$fileName"
            }
            mediaInfoBean.setBigPic(bigPic)
                .setShowOriginalBtn(if (a.isGif) "0" else "1")
                .setType("3")
                .setSrcPic(a.staticImg)
                .setIsGif(if (a.isGif) "1" else "0")
                .setIsLongPic(if (a.height > App.ScreenInfo.EXACT_SCREEN_HEIGHT) "1" else "0")
                .setOriginPic(origin)
            return mediaInfoBean
        }
    }
}
