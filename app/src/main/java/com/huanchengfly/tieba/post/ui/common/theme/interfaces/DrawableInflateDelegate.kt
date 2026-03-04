package com.huanchengfly.tieba.post.ui.common.theme.interfaces

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

interface DrawableInflateDelegate<T : Drawable> {
    @Throws(IOException::class, XmlPullParserException::class)
    fun inflateDrawable(context: Context, parser: XmlPullParser, attrs: AttributeSet): T
}
