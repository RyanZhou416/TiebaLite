package com.huanchengfly.tieba.post.components.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.huanchengfly.tieba.post.R

abstract class BaseFullScreenDialog(context: Context) : Dialog(context, R.style.Dialog_FullScreen) {

    val contentView: View = View.inflate(getContext(), getLayoutId(), null)

    protected abstract fun getLayoutId(): Int

    protected abstract fun initView(contentView: View)

    override fun show() {
        super.show()
        window?.let { win ->
            val layoutParams = win.attributes
            layoutParams.gravity = Gravity.BOTTOM
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            win.decorView.setPadding(0, 0, 0, 0)
            win.attributes = layoutParams
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(contentView)
        initView(contentView)
    }
}
