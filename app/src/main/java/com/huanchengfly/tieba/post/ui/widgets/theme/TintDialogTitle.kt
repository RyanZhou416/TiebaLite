package com.huanchengfly.tieba.post.ui.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.DialogTitle
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ColorStateListUtils
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils

@SuppressLint("CustomViewStyleable", "RestrictedApi")
class TintDialogTitle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DialogTitle(context, attrs, defStyleAttr), Tintable {

    private var mBackgroundTintResId: Int
    private var mTintResId: Int
    private var mTintListResId: Int

    init {
        if (isInEditMode) {
            mBackgroundTintResId = R.color.transparent
            mTintResId = 0
            mTintListResId = 0
        } else if (attrs == null) {
            mBackgroundTintResId = R.color.transparent
            mTintResId = 0
            mTintListResId = 0
            applyTintColor()
        } else {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0)
            mBackgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, R.color.transparent)
            mTintResId = array.getResourceId(R.styleable.TintView_tint, 0)
            mTintListResId = array.getResourceId(R.styleable.TintView_tintList, 0)
            array.recycle()
            applyTintColor()
        }
    }

    fun setBackgroundTintResId(backgroundTintResId: Int) {
        mBackgroundTintResId = backgroundTintResId
        applyTintColor()
    }

    fun setTintResId(tintResId: Int) {
        mTintResId = tintResId
        applyTintColor()
    }

    private fun applyTintColor() {
        if (background == null) {
            setBackgroundColor(ThemeUtils.getColorById(context, mBackgroundTintResId))
        } else {
            background = ThemeUtils.tintDrawable(background, ThemeUtils.getColorById(context, mBackgroundTintResId))
        }
        if (mTintResId != 0 && mTintListResId == 0) {
            setTextColor(ColorStateList.valueOf(ThemeUtils.getColorById(context, mTintResId)))
        } else if (mTintListResId != 0) {
            setTextColor(ColorStateListUtils.createColorStateList(context, mTintListResId))
        }
    }

    override fun tint() {
        applyTintColor()
    }
}
