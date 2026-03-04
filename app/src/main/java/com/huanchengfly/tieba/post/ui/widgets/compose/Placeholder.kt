/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Forked from com.google.accompanist:accompanist-placeholder-material
 */

package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.node.Ref
import androidx.compose.ui.unit.LayoutDirection

interface PlaceholderHighlight {
    val animationSpec: InfiniteRepeatableSpec<Float>
    fun brush(progress: Float, size: Size): Brush
    companion object
}

fun PlaceholderHighlight.Companion.fade(
    highlightColor: Color = Color.Unspecified,
    animationSpec: InfiniteRepeatableSpec<Float> = PlaceholderDefaults.fadeAnimationSpec,
): PlaceholderHighlight = FadePlaceholderHighlight(highlightColor, animationSpec)

object PlaceholderDefaults {
    val fadeAnimationSpec: InfiniteRepeatableSpec<Float> by lazy {
        infiniteRepeatable(
            animation = tween(delayMillis = 200, durationMillis = 600),
            repeatMode = RepeatMode.Reverse,
        )
    }
}

/**
 * Draws a placeholder shape on top of the composable content when [visible] is true.
 *
 * When [color] is [Color.Unspecified], defaults are derived from [MaterialTheme].
 * When [shape] is null, [MaterialTheme.shapes.small] is used.
 */
fun Modifier.placeholder(
    visible: Boolean,
    color: Color = Color.Unspecified,
    shape: Shape? = null,
    highlight: PlaceholderHighlight? = null,
): Modifier = composed {
    if (!visible) return@composed Modifier

    val surfaceColor = MaterialTheme.colors.surface
    val contentColor = contentColorFor(surfaceColor)
        .takeIf { it.isSpecified } ?: MaterialTheme.colors.onSurface

    val resolvedColor = if (color.isSpecified) {
        color
    } else {
        contentColor.copy(alpha = 0.1f).compositeOver(surfaceColor)
    }

    val resolvedHighlight = if (highlight is FadePlaceholderHighlight && !highlight.highlightColor.isSpecified) {
        FadePlaceholderHighlight(
            highlightColor = contentColor.copy(alpha = 0.15f).compositeOver(surfaceColor),
            animationSpec = highlight.animationSpec,
        )
    } else {
        highlight
    }

    val resolvedShape = shape ?: MaterialTheme.shapes.small

    val animationProgress = if (resolvedHighlight != null) {
        val infiniteTransition = rememberInfiniteTransition(label = "placeholder")
        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = resolvedHighlight.animationSpec,
            label = "placeholderHighlight",
        )
        progress
    } else {
        1f
    }

    val lastSize = remember { Ref<Size>() }
    val lastLayoutDirection = remember { Ref<LayoutDirection>() }
    val lastOutline = remember { Ref<Outline>() }

    Modifier.drawWithContent {
        drawContent()

        val outline = lastOutline.value.takeIf {
            size == lastSize.value && layoutDirection == lastLayoutDirection.value
        } ?: resolvedShape.createOutline(size, layoutDirection, this).also {
            lastSize.value = size
            lastLayoutDirection.value = layoutDirection
            lastOutline.value = it
        }

        drawOutline(outline = outline, color = resolvedColor)

        if (resolvedHighlight != null) {
            drawOutline(
                outline = outline,
                brush = resolvedHighlight.brush(animationProgress, size),
            )
        }
    }
}

private class FadePlaceholderHighlight(
    val highlightColor: Color,
    override val animationSpec: InfiniteRepeatableSpec<Float>,
) : PlaceholderHighlight {
    override fun brush(progress: Float, size: Size): Brush {
        return SolidColor(highlightColor.copy(alpha = highlightColor.alpha * progress))
    }
}
