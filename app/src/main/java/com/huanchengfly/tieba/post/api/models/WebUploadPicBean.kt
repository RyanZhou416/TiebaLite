package com.huanchengfly.tieba.post.api.models

import com.huanchengfly.tieba.post.models.BaseBean

class WebUploadPicBean : BaseBean() {
    var errorMsg: String? = null

    var imageBaseSrc: String? = null

    var imageInfo: String? = null

    var imageSrc: String? = null
}
