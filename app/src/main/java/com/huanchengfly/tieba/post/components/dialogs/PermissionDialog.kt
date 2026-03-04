package com.huanchengfly.tieba.post.components.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.interfaces.OnDeniedCallback
import com.huanchengfly.tieba.post.interfaces.OnGrantedCallback
import com.huanchengfly.tieba.post.models.PermissionBean
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil

class PermissionDialog(
    context: Context,
    var permissionBean: PermissionBean
) : AlertDialog(context, false, null), View.OnClickListener {

    private val titleView: TextView
    private val iconView: ImageView
    private val allowBtn: Button
    private val deniedBtn: Button
    private val checkBox: CheckBox
    var onGrantedCallback: OnGrantedCallback? = null
    var onDeniedCallback: OnDeniedCallback? = null

    init {
        val contentView = View.inflate(context, R.layout.dialog_permission, null)
        titleView = contentView.findViewById(R.id.permission_title)
        iconView = contentView.findViewById(R.id.permission_icon)
        allowBtn = contentView.findViewById(R.id.permission_actions_allow)
        deniedBtn = contentView.findViewById(R.id.permission_actions_denied)
        checkBox = contentView.findViewById(R.id.permission_actions_checkbox)
        setView(contentView)
        initView()
    }

    private fun initView() {
        titleView.text = permissionBean.title
        iconView.setImageResource(permissionBean.icon)
        allowBtn.setOnClickListener(this)
        deniedBtn.setOnClickListener(this)
    }

    @SuppressLint("ApplySharedPref")
    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.permission_actions_allow -> {
                onGrantedCallback?.onGranted(checkBox.isChecked)
                if (checkBox.isChecked) {
                    SharedPreferencesUtil.get(v.context, SharedPreferencesUtil.SP_PERMISSION)
                        .edit()
                        .putInt("${permissionBean.data}_${permissionBean.id}", STATE_ALLOW)
                        .commit()
                }
                dismiss()
            }
            R.id.permission_actions_denied -> {
                onDeniedCallback?.onDenied(checkBox.isChecked)
                if (checkBox.isChecked) {
                    SharedPreferencesUtil.get(v.context, SharedPreferencesUtil.SP_PERMISSION)
                        .edit()
                        .putInt("${permissionBean.data}_${permissionBean.id}", STATE_DENIED)
                        .commit()
                }
                dismiss()
            }
        }
    }

    override fun show() {
        val state = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_PERMISSION)
            .getInt("${permissionBean.data}_${permissionBean.id}", STATE_UNSET)
        when (state) {
            STATE_UNSET -> super.show()
            STATE_ALLOW -> onGrantedCallback?.onGranted(true)
            STATE_DENIED -> onDeniedCallback?.onDenied(true)
        }
    }

    object CustomPermission {
        const val PERMISSION_LOCATION = 0
        const val PERMISSION_START_APP = 1
        const val PERMISSION_CLIPBOARD_COPY = 2
    }

    companion object {
        const val STATE_DENIED = 2
        const val STATE_ALLOW = 1
        const val STATE_UNSET = 0
    }
}
