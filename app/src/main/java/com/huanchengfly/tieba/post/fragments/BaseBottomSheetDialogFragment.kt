package com.huanchengfly.tieba.post.fragments

import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.Util

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    protected var bottomSheetDialog: BottomSheetDialog? = null
    protected var mBehavior: BottomSheetBehavior<*>? = null
    var rootView: View? = null
        protected set
    private var attachContext: Context? = null

    @TargetApi(23)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        onAttachToContext(context)
    }

    @Suppress("deprecation")
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity)
        }
    }

    @CallSuper
    private fun onAttachToContext(context: Context) {
        attachContext = context
    }

    protected fun getAttachContext(): Context =
        attachContext ?: requireContext()

    protected fun getScreenHeight(): Int =
        getAttachContext().resources.displayMetrics.heightPixels

    protected fun getStatusBarHeight(): Int {
        val resources = getAttachContext().resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    protected open fun isFullScreen(): Boolean = false

    override fun onStart() {
        super.onStart()
        dialog?.let { dlg ->
            val bottomSheet = dlg.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height =
                if (isFullScreen()) ViewGroup.LayoutParams.MATCH_PARENT
                else ViewGroup.LayoutParams.WRAP_CONTENT
        }
        view?.post {
            val parent = view?.parent as? View ?: return@post
            val params = parent.layoutParams as? CoordinatorLayout.LayoutParams ?: return@post
            val behavior = params.behavior as? BottomSheetBehavior<*>
            behavior?.setPeekHeight(view?.measuredHeight ?: 0)
        }
    }

    protected abstract fun initView()

    open fun resetView() {}

    fun isShowing(): Boolean = bottomSheetDialog?.isShowing == true

    fun close() {
        dismiss()
    }

    protected open fun onCreatedBehavior(behavior: BottomSheetBehavior<*>) {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dlg = BottomSheetDialog(getAttachContext(), R.style.BottomSheetDialogStyle)
        bottomSheetDialog = dlg

        if (rootView == null) {
            rootView = Util.inflate(getAttachContext(), getLayoutId())
        }
        resetView()

        val rv = rootView ?: return dlg
        dlg.setContentView(rv)
        mBehavior = dlg.behavior.also {
            it.isHideable = true
            onCreatedBehavior(it)
        }
        dlg.window?.let { window ->
            if (needFixHeight()) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, getHeight())
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            (rv.parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
            window.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.setBackgroundColor(Color.TRANSPARENT)
        }

        initView()
        return dlg
    }

    protected open fun getHeight(): Int {
        val dialogHeight = getScreenHeight() - getStatusBarHeight()
        return if (dialogHeight == 0) ViewGroup.LayoutParams.MATCH_PARENT else dialogHeight
    }

    protected abstract fun getLayoutId(): Int

    protected open fun needFixHeight(): Boolean = true

    companion object {
        const val TAG = "BaseBottomSheetDialog"
    }
}
