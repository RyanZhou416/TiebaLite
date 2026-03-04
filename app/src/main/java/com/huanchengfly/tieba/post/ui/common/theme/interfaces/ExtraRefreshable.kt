package com.huanchengfly.tieba.post.ui.common.theme.interfaces

import android.app.Activity
import android.view.View

interface ExtraRefreshable {
    fun refreshGlobal(activity: Activity)
    fun refreshSpecificView(view: View)
}
