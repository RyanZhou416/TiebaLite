package com.huanchengfly.tieba.post.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.huanchengfly.tieba.post.R

object DialogUtil {
    @JvmStatic
    fun build(context: Context): AlertDialog.Builder = AlertDialog.Builder(context)

    @JvmStatic
    fun buildBottomDialog(context: Context): AlertDialog.Builder =
        AlertDialog.Builder(context, R.style.Dialog_Bottom)
}
