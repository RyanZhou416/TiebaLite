package com.huanchengfly.tieba.post.ui.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.Toolbar
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils

class TintToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.toolbarStyle
) : Toolbar(context, attrs, defStyleAttr), Tintable {

    private var mBackgroundTintResId: Int
    private var mItemTintResId: Int
    private var mSecondaryItemTintResId: Int
    private var mActiveItemTintResId: Int

    init {
        if (isInEditMode) {
            mBackgroundTintResId = 0
            mItemTintResId = R.color.default_color_toolbar_item
            mSecondaryItemTintResId = R.color.default_color_toolbar_item_secondary
            mActiveItemTintResId = R.color.default_color_toolbar_item_active
        } else if (attrs == null) {
            mBackgroundTintResId = 0
            mItemTintResId = R.color.default_color_toolbar_item
            mSecondaryItemTintResId = R.color.default_color_toolbar_item_secondary
            mActiveItemTintResId = R.color.default_color_toolbar_item_active
            applyTintColor()
        } else {
            @SuppressLint("CustomViewStyleable")
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.TintToolbar, defStyleAttr, 0)
            mBackgroundTintResId = array.getResourceId(R.styleable.TintToolbar_toolbarBackgroundTint, 0)
            mItemTintResId = array.getResourceId(R.styleable.TintToolbar_itemTint, R.color.default_color_toolbar_item)
            mSecondaryItemTintResId = array.getResourceId(R.styleable.TintToolbar_secondaryItemTint, R.color.default_color_toolbar_item_secondary)
            mActiveItemTintResId = array.getResourceId(R.styleable.TintToolbar_activeItemTint, R.color.default_color_toolbar_item_active)
            array.recycle()
            applyTintColor()
        }
    }

    override fun tint() {
        applyTintColor()
    }

    private fun applyTintColor() {
        setTitleTextAppearance(context, R.style.TextAppearance_Title)
        setSubtitleTextAppearance(context, R.style.TextAppearance_Subtitle)
        fixColor()
        tintBackground()
        tintNavigationIcon()
        tintOverflowIcon()
        tintMenuIcon()
        setTitleTextColor(ThemeUtils.getColorById(context, mItemTintResId))
        setSubtitleTextColor(ThemeUtils.getColorById(context, mSecondaryItemTintResId))
    }

    private fun fixColor() {
        if (mItemTintResId == 0) {
            mItemTintResId = R.color.default_color_toolbar_item
        }
        if (mSecondaryItemTintResId == 0) {
            mSecondaryItemTintResId = R.color.default_color_toolbar_item_secondary
        }
        if (mActiveItemTintResId == 0) {
            mActiveItemTintResId = R.color.default_color_toolbar_item_active
        }
    }

    private fun tintBackground() {
        if (mBackgroundTintResId != 0) {
            if (background == null) {
                setBackgroundColor(ThemeUtils.getColorById(context, mBackgroundTintResId))
            } else {
                backgroundTintList = ColorStateList.valueOf(ThemeUtils.getColorById(context, mBackgroundTintResId))
            }
        }
    }

    override fun setNavigationIcon(resId: Int) {
        super.setNavigationIcon(resId)
        applyTintColor()
    }

    private fun tintMenuIcon() {
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            val drawable = menuItem.icon ?: continue
            val states = arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_enabled),
                intArrayOf()
            )
            val colorStateList = ColorStateList(
                states, intArrayOf(
                    ThemeUtils.getColorById(context, mActiveItemTintResId),
                    ThemeUtils.getColorById(context, mItemTintResId),
                    ThemeUtils.getColorById(context, mSecondaryItemTintResId)
                )
            )
            drawable.setTintList(colorStateList)
            drawable.invalidateSelf()
            menuItem.icon = drawable
            Log.i(TAG, "tintMenuIcon: ${i}finish")
        }
    }

    private fun tintNavigationIcon() {
        val drawable = navigationIcon ?: return
        navigationIcon = ThemeUtils.tintDrawable(drawable, ThemeUtils.getColorById(context, mItemTintResId))
    }

    private fun tintOverflowIcon() {
        val drawable = overflowIcon ?: return
        overflowIcon = ThemeUtils.tintDrawable(drawable, ThemeUtils.getColorById(context, mItemTintResId))
    }

    override fun inflateMenu(resId: Int) {
        super.inflateMenu(resId)
        applyTintColor()
    }

    fun setBackgroundTintResId(backgroundTintResId: Int) {
        mBackgroundTintResId = backgroundTintResId
        tint()
    }

    fun setItemTintResId(itemTintResId: Int) {
        mItemTintResId = itemTintResId
        tint()
    }

    fun setSecondaryItemTintResId(secondaryItemTintResId: Int) {
        mSecondaryItemTintResId = secondaryItemTintResId
        tint()
    }

    fun setActiveItemTintResId(activeItemTintResId: Int) {
        mActiveItemTintResId = activeItemTintResId
        tint()
    }

    companion object {
        const val TAG = "TintToolbar"
    }
}
