package com.huanchengfly.tieba.post.models

import androidx.annotation.DrawableRes

data class PermissionBean(
    var id: Int,
    var data: String,
    var title: String,
    @DrawableRes var icon: Int
)
