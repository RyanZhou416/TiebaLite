package com.huanchengfly.tieba.post.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Build
import android.text.TextUtils
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.MainActivityV2
import com.huanchengfly.tieba.post.pendingIntentFlagMutable

object CrashUtil {
    const val TAG = "CrashUtil"

    @JvmStatic
    fun newCrash(context: Context, throwable: Throwable) {
        val time = getTime(context)
        saveException(context, throwable)
        if (System.currentTimeMillis() - time > 30 * 1000L) restart(context)
    }

    private fun getLocalPackageInfo(context: Context): PackageInfo? {
        return try {
            context.applicationContext.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getCrashReport(context: Context, ex: Throwable?): String {
        val exceptionStr = StringBuilder()
        val pinfo = getLocalPackageInfo(context) ?: return ""
        if (ex != null) {
            exceptionStr.append("App Version：${pinfo.versionName}")
            exceptionStr.append("_${pinfo.versionCode}\n")
            exceptionStr.append("OS Version：${Build.VERSION.RELEASE}")
            exceptionStr.append("_")
            exceptionStr.append("${Build.VERSION.SDK_INT}\n")
            exceptionStr.append("Vendor: ${Build.MANUFACTURER}\n")
            exceptionStr.append("Model: ${Build.MODEL}\n")
            var errorStr = ex.localizedMessage
            if (TextUtils.isEmpty(errorStr)) {
                errorStr = ex.message
            }
            if (TextUtils.isEmpty(errorStr)) {
                errorStr = ex.toString()
            }
            exceptionStr.append("Exception: $errorStr\n")
            val elements = ex.stackTrace
            for (element in elements) {
                exceptionStr.append("$element\n")
            }
        } else {
            exceptionStr.append("no exception. Throwable is null\n")
        }
        return exceptionStr.toString()
    }

    @Suppress("ApplySharedPref")
    private fun saveException(context: Context, throwable: Throwable) {
        context.getSharedPreferences("crash", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .putLong("time", System.currentTimeMillis())
            .putString("message", getCrashMessage(throwable))
            .putString("crash", getCrashReport(context, throwable))
            .commit()
    }

    private fun getCrashMessage(ex: Throwable): String {
        var errorStr = ex.localizedMessage
        if (TextUtils.isEmpty(errorStr)) {
            errorStr = ex.message
        }
        if (TextUtils.isEmpty(errorStr)) {
            errorStr = ex.toString()
        }
        return errorStr ?: ex.toString()
    }

    @JvmStatic
    fun getCrashMessage(context: Context): String? =
        context.getSharedPreferences("crash", Context.MODE_PRIVATE)
            .getString("message", "")

    @Suppress("WrongConstant")
    private fun restart(context: Context) {
        val intent = Intent(context.applicationContext, MainActivityV2::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.let {
            val restartIntent = PendingIntent.getActivity(
                context.applicationContext,
                0,
                intent,
                pendingIntentFlagMutable()
            )
            it.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent)
        }
        (context.applicationContext as App).removeAllActivity()
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    @JvmStatic
    fun getTime(context: Context): Long =
        context.getSharedPreferences("crash", Context.MODE_PRIVATE).getLong("time", 0L)

    @JvmStatic
    fun getCrash(context: Context): String? =
        context.getSharedPreferences("crash", Context.MODE_PRIVATE).getString("crash", null)

    @JvmStatic
    fun clear(context: Context) {
        context.getSharedPreferences("crash", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {
        @Volatile
        var crashing = false
        private var defaultHandler: Thread.UncaughtExceptionHandler? = null
        private lateinit var mContext: Context

        fun init(context: Context) {
            mContext = context
            defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(this)
        }

        override fun uncaughtException(t: Thread, e: Throwable) {
            if (crashing) return
            crashing = true
            e.printStackTrace()
            if (!handleException(e)) {
                defaultHandler?.uncaughtException(t, e)
            }
        }

        private fun handleException(e: Throwable?): Boolean {
            if (e == null) return false
            return try {
                newCrash(mContext, e)
                true
            } catch (ex: Exception) {
                false
            }
        }

        companion object {
            @SuppressLint("StaticFieldLeak")
            @Volatile
            private var sInstance: CrashHandler? = null

            @JvmStatic
            fun getInstance(): CrashHandler {
                return sInstance ?: synchronized(CrashHandler::class.java) {
                    sInstance ?: CrashHandler().also { sInstance = it }
                }
            }
        }
    }
}
