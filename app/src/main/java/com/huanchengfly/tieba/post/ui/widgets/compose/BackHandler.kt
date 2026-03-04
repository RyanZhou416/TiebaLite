package com.huanchengfly.tieba.post.ui.widgets.compose

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.huanchengfly.tieba.post.LocalDestination
import com.ramcosta.composedestinations.spec.DestinationSpec

@SuppressLint("RestrictedApi")
@Composable
fun MyBackHandler(
    enabled: Boolean,
    currentScreen: DestinationSpec? = null,
    onBack: () -> Unit,
) {
    val currentDestination = LocalDestination.current

    val shouldEnable =
        enabled && (currentScreen == null || currentDestination?.route == currentScreen.route)

    BackHandler(enabled = shouldEnable, onBack = onBack)
}