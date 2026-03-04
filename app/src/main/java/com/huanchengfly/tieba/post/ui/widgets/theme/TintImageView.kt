package com.huanchengfly.tieba.post.ui.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.interfaces.BackgroundTintable
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ColorStateListUtils

@SuppressLint("CustomViewStyleable")
class TintImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), Tintable, BackgroundTintable {

    private var mTintListResId: Int
    private var mBackgroundTintResId: Int

    init {
        if (isInEditMode) {
            mBackgroundTintResId = 0
            mTintListResId = 0
        } else if (attrs == null) {
            mBackgroundTintResId = 0
            mTintListResId = 0
            applyTintColor()
        } else {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.TintImageView, defStyleAttr, 0)
            mBackgroundTintResId = array.getResourceId(R.styleable.TintImageView_backgroundTint, 0)
            mTintListResId = array.getResourceId(R.styleable.TintImageView_tint, 0)
            array.recycle()
            applyTintColor()
        }
    }

    fun setTintListResId(tintListResId: Int) {
        mTintListResId = tintListResId
        applyTintColor()
    }

    private fun applyTintColor() {
        if (mBackgroundTintResId != 0) {
            if (background == null) {
                background = ColorDrawable(Color.BLACK)
            }
            backgroundTintList = ColorStateListUtils.createColorStateList(context, mBackgroundTintResId)
        }
        if (mTintListResId != 0) {
            imageTintList = ColorStateListUtils.createColorStateList(context, mTintListResId)
        }
    }

    override fun tint() {
        applyTintColor()
    }

    override fun getBackgroundTintResId(): Int = mBackgroundTintResId

    override fun setBackgroundTintResId(resId: Int) {
        mBackgroundTintResId = resId
        applyTintColor()
    }
}
