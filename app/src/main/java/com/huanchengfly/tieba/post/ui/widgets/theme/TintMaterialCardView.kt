package com.huanchengfly.tieba.post.ui.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.google.android.material.card.MaterialCardView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils

@SuppressLint("CustomViewStyleable")
class TintMaterialCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr), Tintable {

    init {
        applyTintColor()
    }

    override fun tint() {
        applyTintColor()
    }

    private fun applyTintColor() {
        val bg = ThemeUtils.getColorById(context, R.color.default_color_card)
        setCardBackgroundColor(bg)
        setStrokeColor(ThemeUtils.getColorById(context, R.color.default_color_divider))
    }

    companion object {
        private const val TAG = "TintMaterialCardView"
    }
}
