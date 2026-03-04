package com.huanchengfly.tieba.post.models

import android.content.Context
import android.net.Uri
import com.huanchengfly.tieba.post.api.models.UploadResultBean
import com.huanchengfly.tieba.post.api.models.WebUploadPicBean
import com.huanchengfly.tieba.post.utils.FileUtil
import java.io.File

class PhotoInfoBean @JvmOverloads constructor(
    context: Context,
    var fileUri: Uri,
    var uploadResult: UploadResultBean? = null
) {
    var file: File? = null
        private set

    var webUploadPicBean: WebUploadPicBean? = null

    init {
        try {
            file = File(FileUtil.getRealPathFromUri(context, fileUri))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val TAG = "PhotoInfoBean"
    }
}
