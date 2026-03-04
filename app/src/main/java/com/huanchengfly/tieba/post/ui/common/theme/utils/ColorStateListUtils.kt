package com.huanchengfly.tieba.post.ui.common.theme.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.StateSet
import android.util.TypedValue
import android.util.Xml
import androidx.core.graphics.ColorUtils
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.LinkedList

object ColorStateListUtils {

    @JvmStatic
    fun createColorStateList(context: Context, resId: Int): ColorStateList? {
        if (resId <= 0) return null

        val value = TypedValue()
        context.resources.getValue(resId, value, true)

        if (value.type in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT) {
            return ColorStateList.valueOf(ThemeUtils.getColorById(context, value.resourceId))
        }

        val file = value.string?.toString() ?: return null
        try {
            if (file.endsWith("xml")) {
                val rp = context.resources.assets.openXmlResourceParser(value.assetCookie, file)
                val attrs = Xml.asAttributeSet(rp)
                var type: Int

                while (rp.next().also { type = it } != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT
                ) {
                    // Seek parser to start tag.
                }

                if (type != XmlPullParser.START_TAG) {
                    throw XmlPullParserException("No start tag found")
                }

                val cl = createFromXmlInner(context, rp, attrs)
                rp.close()
                return cl
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun createFromXmlInner(
        context: Context,
        parser: XmlPullParser,
        attrs: android.util.AttributeSet
    ): ColorStateList? {
        val name = parser.name
        if (name != "selector") {
            throw XmlPullParserException(
                "${parser.positionDescription}: invalid color state list tag $name"
            )
        }
        return inflateColorStateList(context, parser, attrs)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun inflateColorStateList(
        context: Context,
        parser: XmlPullParser,
        attrs: android.util.AttributeSet
    ): ColorStateList? {
        val innerDepth = parser.depth + 1
        var depth: Int = 0
        var type: Int

        val stateList = LinkedList<IntArray>()
        val colorList = LinkedList<Int>()

        while (parser.next().also { type = it } != XmlPullParser.END_DOCUMENT
            && (parser.depth.also { depth = it } >= innerDepth || type != XmlPullParser.END_TAG)
        ) {
            if (type != XmlPullParser.START_TAG || depth > innerDepth
                || parser.name != "item"
            ) {
                continue
            }

            val a1 = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.color))
            val value = a1.getResourceId(0, Color.MAGENTA)
            val baseColor =
                if (value == Color.MAGENTA) Color.MAGENTA else ThemeUtils.getColorById(context, value)
            a1.recycle()

            val a2 = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.alpha))
            val alphaMod = a2.getFloat(0, 1.0f)
            a2.recycle()

            colorList.add(
                if (alphaMod != 1.0f) {
                    ColorUtils.setAlphaComponent(
                        baseColor,
                        Math.round(Color.alpha(baseColor) * alphaMod)
                    )
                } else {
                    baseColor
                }
            )

            stateList.add(extractStateSet(attrs))
        }

        if (stateList.size > 0 && stateList.size == colorList.size) {
            val colors = colorList.toIntArray()
            return ColorStateList(stateList.toTypedArray(), colors)
        }
        return null
    }

    private fun extractStateSet(attrs: android.util.AttributeSet): IntArray {
        var j = 0
        val numAttrs = attrs.attributeCount
        val states = IntArray(numAttrs)
        for (i in 0 until numAttrs) {
            val stateResId = attrs.getAttributeNameResource(i)
            when (stateResId) {
                0 -> {}
                android.R.attr.color, android.R.attr.alpha -> continue
                else -> {
                    states[j++] = if (attrs.getAttributeBooleanValue(i, false)) {
                        stateResId
                    } else {
                        -stateResId
                    }
                }
            }
        }
        return StateSet.trimStateSet(states, j)
    }
}
