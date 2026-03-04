package com.huanchengfly.tieba.post.components.prefs

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference

class TimePickerPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {

    var hour: Int = 0
    var minute: Int = 0

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val value = getPersistedString((defaultValue as? String) ?: "00:00")
        hour = parseHour(value)
        minute = parseMinute(value)
    }

    fun persistStringValue(value: String) {
        persistString(value)
    }

    companion object {
        @JvmStatic
        fun parseHour(value: String): Int {
            return try {
                value.split(":")[0].toInt()
            } catch (e: Exception) {
                0
            }
        }

        @JvmStatic
        fun parseMinute(value: String): Int {
            return try {
                value.split(":")[1].toInt()
            } catch (e: Exception) {
                0
            }
        }

        @JvmStatic
        fun timeToString(h: Int, m: Int): String {
            return String.format("%02d", h) + ":" + String.format("%02d", m)
        }
    }
}
