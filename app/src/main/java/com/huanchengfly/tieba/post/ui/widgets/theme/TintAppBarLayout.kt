package com.huanchengfly.tieba.post.ui.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.appbar.AppBarLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils

@SuppressLint("CustomViewStyleable")
class TintAppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr), Tintable {

    private var mBackgroundTintResId: Int

    init {
        if (isInEditMode) {
            mBackgroundTintResId = R.color.default_color_background
        } else if (attrs == null) {
            mBackgroundTintResId = R.color.default_color_background
            applyTintColor()
        } else {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0)
            mBackgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, R.color.default_color_background)
            array.recycle()
            applyTintColor()
        }
    }

    override fun tint() {
        applyTintColor()
    }

    private fun applyTintColor() {
        if (background == null) {
            setBackgroundColor(ThemeUtils.getColorById(context, mBackgroundTintResId))
        } else {
            backgroundTintList = ColorStateList.valueOf(ThemeUtils.getColorById(context, mBackgroundTintResId))
        }
    }
}
