package com.huanchengfly.tieba.post.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.huanchengfly.tieba.post.interfaces.BackHandledInterface

object HandleBackUtil {
    @JvmStatic
    fun handleBackPress(fragmentManager: FragmentManager): Boolean {
        val fragments = fragmentManager.fragments
        for (i in fragments.size - 1 downTo 0) {
            val child = fragments[i]
            if (isFragmentBackHandled(child)) {
                return true
            }
        }
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            return true
        }
        return false
    }

    @JvmStatic
    fun handleBackPress(fragment: Fragment): Boolean =
        handleBackPress(fragment.childFragmentManager)

    @JvmStatic
    fun handleBackPress(fragmentActivity: FragmentActivity): Boolean =
        handleBackPress(fragmentActivity.supportFragmentManager)

    @JvmStatic
    @Suppress("DEPRECATION")
    fun isFragmentBackHandled(fragment: Fragment?): Boolean {
        return fragment != null
                && fragment.isVisible
                && fragment.userVisibleHint
                && fragment is BackHandledInterface
                && fragment.onBackPressed()
    }
}
