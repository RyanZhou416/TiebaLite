package com.huanchengfly.tieba.post.ui.widgets.theme

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.navigation.NavigationView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils

class TintNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NavigationView(context, attrs, defStyleAttr), Tintable {

    private var mBackgroundTintResId: Int
    private var mItemIconTintResId: Int
    private var mItemTextTintResId: Int

    init {
        if (isInEditMode) {
            mBackgroundTintResId = R.color.transparent
            mItemIconTintResId = 0
            mItemTextTintResId = 0
        } else if (attrs == null) {
            mBackgroundTintResId = R.color.transparent
            mItemIconTintResId = 0
            mItemTextTintResId = 0
            applyTintColor()
        } else {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.TintNavigationView, defStyleAttr, 0)
            mBackgroundTintResId = array.getResourceId(R.styleable.TintNavigationView_navigationBackgroundTint, R.color.transparent)
            mItemIconTintResId = array.getResourceId(R.styleable.TintNavigationView_itemIconTint, 0)
            mItemTextTintResId = array.getResourceId(R.styleable.TintNavigationView_itemTextTint, 0)
            array.recycle()
            applyTintColor()
        }
    }

    private fun applyTintColor() {
        if (background == null) {
            setBackgroundColor(ThemeUtils.getColorById(context, mBackgroundTintResId))
        } else {
            backgroundTintList = ColorStateList.valueOf(ThemeUtils.getColorById(context, mBackgroundTintResId))
        }
        if (mItemIconTintResId != 0) {
            itemIconTintList = ColorStateList.valueOf(ThemeUtils.getColorById(context, mItemIconTintResId))
        } else {
            itemIconTintList = null
        }
        if (mItemTextTintResId != 0) {
            itemTextColor = ColorStateList.valueOf(ThemeUtils.getColorById(context, mItemTextTintResId))
        } else {
            itemTextColor = null
        }
    }

    override fun tint() {
        applyTintColor()
    }
}
