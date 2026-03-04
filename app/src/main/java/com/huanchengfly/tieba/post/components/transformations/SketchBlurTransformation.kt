package com.huanchengfly.tieba.post.components.transformations

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.github.panpf.sketch.BitmapImage
import com.github.panpf.sketch.Image
import com.github.panpf.sketch.asImage
import com.github.panpf.sketch.request.RequestContext
import com.github.panpf.sketch.transform.TransformResult
import com.github.panpf.sketch.transform.Transformation
import com.huanchengfly.tieba.post.components.transformations.internal.FastBlur
import com.huanchengfly.tieba.post.components.transformations.internal.RSBlur
import com.huanchengfly.tieba.post.components.transformations.internal.SupportRSBlur

@Suppress("USELESS_ELVIS")
internal val Bitmap.safeConfig: Bitmap.Config
    get() = config ?: Bitmap.Config.ARGB_8888

@Deprecated("Use com.github.panpf.sketch.transform.BlurTransformation instead")
class SketchBlurTransformation(
    val radius: Int = 25,
    private val sampling: Int = 1,
) : Transformation {
    override val key: String = "SketchBlurTransformation(radius=$radius, sampling=$sampling)"

    override fun transform(
        requestContext: RequestContext,
        input: Image,
    ): TransformResult? {
        val inputBitmap = (input as? BitmapImage)?.bitmap ?: return null
        val context = requestContext.request.context

        val width: Int = inputBitmap.width
        val height: Int = inputBitmap.height
        val scaledWidth = width / sampling
        val scaledHeight = height / sampling

        var bitmap = Bitmap.createBitmap(
            scaledWidth,
            scaledHeight,
            inputBitmap.safeConfig,
        ).apply {
            density = inputBitmap.density
        }

        val canvas = Canvas(bitmap)
        canvas.scale(1 / sampling.toFloat(), 1 / sampling.toFloat())
        val paint = Paint()
        paint.flags = Paint.FILTER_BITMAP_FLAG
        canvas.drawBitmap(inputBitmap, 0f, 0f, paint)

        bitmap = try {
            SupportRSBlur.blur(context, bitmap, radius)
        } catch (e: NoClassDefFoundError) {
            RSBlur.blur(context, bitmap, radius)
        } catch (e: RuntimeException) {
            FastBlur.blur(bitmap, radius, true)!!
        }

        return TransformResult(bitmap.asImage(), key)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SketchBlurTransformation) return false
        return radius == other.radius && sampling == other.sampling
    }

    override fun hashCode(): Int {
        var result = radius
        result = 31 * result + sampling
        return result
    }

    override fun toString(): String = key
}
