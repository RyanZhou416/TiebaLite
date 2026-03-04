package com.huanchengfly.tieba.post.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.MenuRes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.interfaces.InitMenuCallback

class MenuDialogFragment : BaseBottomSheetDialogFragment(),
    NavigationView.OnNavigationItemSelectedListener {

    private var menuRes: Int = -1
    private var title: String? = null
    private var titleView: TextView? = null
    private var onNavigationItemSelectedListener: NavigationView.OnNavigationItemSelectedListener? = null
    private var navigationView: NavigationView? = null
    var initMenuCallback: InitMenuCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            menuRes = bundle.getInt("menuRes", -1)
            title = bundle.getString("title", null)
        }
    }

    fun setOnNavigationItemSelectedListener(
        listener: NavigationView.OnNavigationItemSelectedListener?
    ): MenuDialogFragment {
        onNavigationItemSelectedListener = listener
        return this
    }

    fun setInitMenuCallback(callback: InitMenuCallback?): MenuDialogFragment {
        initMenuCallback = callback
        return this
    }

    @Suppress("deprecation")
    override fun onCreatedBehavior(behavior: BottomSheetBehavior<*>) {
        behavior.isHideable = false
        behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    close()
                } else if (newState != BottomSheetBehavior.STATE_COLLAPSED) {
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    override fun initView() {
        val rv = rootView ?: return
        navigationView = rv.findViewById(R.id.navigation_view)
        titleView = rv.findViewById<TextView>(R.id.title_text_view).also {
            it.text = title
            it.visibility = if (title == null) View.GONE else View.VISIBLE
        }
        navigationView?.let { navView ->
            navView.inflateMenu(menuRes)
            initMenuCallback?.init(navView.menu)
            navView.setNavigationItemSelectedListener(this)
            navView.post {
                mBehavior?.setPeekHeight(
                    (titleView?.height ?: 0) + navView.height,
                    false
                )
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_menu_dialog

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        onNavigationItemSelectedListener?.let {
            close()
            return it.onNavigationItemSelected(item)
        }
        return false
    }

    override fun getHeight(): Int = ViewGroup.LayoutParams.MATCH_PARENT

    companion object {
        fun newInstance(@MenuRes menuRes: Int, title: String?): MenuDialogFragment {
            return MenuDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt("menuRes", menuRes)
                    putString("title", title)
                }
            }
        }
    }
}
