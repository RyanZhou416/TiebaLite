package com.huanchengfly.tieba.post.utils

import android.content.Context

object JobServiceUtil {
    @JvmStatic
    fun getJobId(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("appData", Context.MODE_PRIVATE)
        var jobId = sharedPreferences.getInt("jobId", -1)
        if (jobId == -1) {
            jobId = (Math.random() * (99999 + 1)).toInt()
            sharedPreferences.edit()
                .putInt("jobId", jobId)
                .apply()
        }
        return jobId
    }
}
