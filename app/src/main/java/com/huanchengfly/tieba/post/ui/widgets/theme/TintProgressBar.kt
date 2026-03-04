package com.huanchengfly.tieba.post.ui.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.core.widget.ContentLoadingProgressBar
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils

@SuppressLint("CustomViewStyleable")
class TintProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ContentLoadingProgressBar(context, attrs), Tintable {

    private var mBackgroundTintResId: Int
    private var mProgressTintResId: Int
    private var mProgressBackgroundTintResId: Int

    init {
        if (isInEditMode) {
            mBackgroundTintResId = 0
            mProgressTintResId = R.color.default_color_primary
            mProgressBackgroundTintResId = 0
        } else if (attrs == null) {
            mBackgroundTintResId = 0
            mProgressTintResId = R.color.default_color_primary
            mProgressBackgroundTintResId = 0
            applyTintColor()
        } else {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.TintSeekbar, 0, 0)
            mBackgroundTintResId = array.getResourceId(R.styleable.TintSeekbar_seekbarBackgroundTint, 0)
            mProgressTintResId = array.getResourceId(R.styleable.TintSeekbar_progressTint, R.color.default_color_primary)
            mProgressBackgroundTintResId = array.getResourceId(R.styleable.TintSeekbar_progressBackgroundTint, 0)
            array.recycle()
            applyTintColor()
        }
    }

    override fun tint() {
        applyTintColor()
    }

    private fun applyTintColor() {
        if (mBackgroundTintResId != 0) {
            if (background == null) {
                setBackgroundColor(ThemeUtils.getColorById(context, mBackgroundTintResId))
            } else {
                backgroundTintList = ColorStateList.valueOf(ThemeUtils.getColorById(context, mBackgroundTintResId))
            }
        }
        progressTintList = ColorStateList.valueOf(ThemeUtils.getColorById(context, mProgressTintResId))
        indeterminateTintList = ColorStateList.valueOf(ThemeUtils.getColorById(context, mProgressTintResId))
        if (mProgressBackgroundTintResId != 0) {
            progressBackgroundTintList = ColorStateList.valueOf(ThemeUtils.getColorById(context, mProgressBackgroundTintResId))
        }
    }

    fun setBackgroundTintResId(backgroundTintResId: Int): TintProgressBar {
        mBackgroundTintResId = backgroundTintResId
        tint()
        return this
    }

    fun setProgressTintResId(progressTintResId: Int): TintProgressBar {
        mProgressTintResId = progressTintResId
        tint()
        return this
    }

    fun setProgressBackgroundTintResId(progressBackgroundTintResId: Int): TintProgressBar {
        mProgressBackgroundTintResId = progressBackgroundTintResId
        tint()
        return this
    }
}
