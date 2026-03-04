package com.huanchengfly.tieba.post.components.dialogs

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.huanchengfly.tieba.post.R

class LoadingDialog(context: Context) : AlertDialog(context) {

    private val loadingTipView: TextView

    init {
        val contentView = View.inflate(context, R.layout.dialog_loading, null)
        loadingTipView = contentView.findViewById(R.id.dialog_loading_tip)
        setCancelable(false)
        setView(contentView)
        setTipText(R.string.text_loading)
    }

    fun setTipText(@StringRes resId: Int) {
        loadingTipView.setText(resId)
    }

    fun setTipText(text: String) {
        loadingTipView.text = text
    }
}
