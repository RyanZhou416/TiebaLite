package com.huanchengfly.tieba.post.ui.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ColorStateListUtils
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils

@SuppressLint("CustomViewStyleable")
class TintSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatSeekBar(context, attrs, defStyleAttr), Tintable {

    private var mBackgroundTintResId: Int
    private var mProgressTintResId: Int
    private var mProgressBackgroundTintResId: Int
    private var mThumbColorResId: Int

    init {
        if (isInEditMode) {
            mBackgroundTintResId = 0
            mProgressTintResId = 0
            mProgressBackgroundTintResId = 0
            mThumbColorResId = 0
        } else if (attrs == null) {
            mBackgroundTintResId = 0
            mProgressTintResId = 0
            mProgressBackgroundTintResId = 0
            mThumbColorResId = 0
            applyTintColor()
        } else {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.TintSeekbar, defStyleAttr, 0)
            mBackgroundTintResId = array.getResourceId(R.styleable.TintSeekbar_seekbarBackgroundTint, 0)
            mProgressTintResId = array.getResourceId(R.styleable.TintSeekbar_progressTint, 0)
            mProgressBackgroundTintResId = array.getResourceId(R.styleable.TintSeekbar_progressBackgroundTint, 0)
            mThumbColorResId = array.getResourceId(R.styleable.TintSeekbar_thumbColor, 0)
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
        if (mProgressTintResId != 0) {
            progressTintList = ColorStateList.valueOf(ThemeUtils.getColorById(context, mProgressTintResId))
        }
        if (mProgressBackgroundTintResId != 0) {
            progressBackgroundTintList = ColorStateList.valueOf(ThemeUtils.getColorById(context, mProgressBackgroundTintResId))
        }
        if (mThumbColorResId != 0) {
            thumbTintList = ColorStateListUtils.createColorStateList(context, mThumbColorResId)
        }
    }
}
