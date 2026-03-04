package com.huanchengfly.tieba.post.components.dialogs

import android.content.Context
import android.view.View
import android.widget.TextView
import com.huanchengfly.tieba.post.R

class CopyTextDialog(context: Context, private val text: String) : BaseFullScreenDialog(context) {

    override fun getLayoutId(): Int = R.layout.dialog_copy_text

    override fun initView(contentView: View) {
        val textView = contentView.findViewById<TextView>(R.id.dialog_copy_text)
        textView.text = text
        textView.setTextIsSelectable(true)
    }

    companion object {
        @JvmField
        val TAG: String = CopyTextDialog::class.java.simpleName
    }
}
