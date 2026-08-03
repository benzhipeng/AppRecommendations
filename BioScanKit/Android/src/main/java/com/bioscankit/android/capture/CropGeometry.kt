package com.bioscankit.android.capture

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.max
import kotlin.math.min

@Immutable
data class CropLayout(
    val canvasFrame: Rect,
    val cropRect: Rect,
    val baseScale: Float,
    val minimumZoom: Float,
)

object CropGeometry {
    fun normalizedSourceRectForAspectFill(
        previewRect: Rect,
        previewSize: Size,
        sourceSize: Size,
    ): Rect {
        if (previewSize.width <= 0f || previewSize.height <= 0f ||
            sourceSize.width <= 0f || sourceSize.height <= 0f
        ) return Rect.Zero

        val visible = previewRect.intersect(Rect(Offset.Zero, previewSize))
        if (visible.width <= 0f || visible.height <= 0f) return Rect.Zero
        val scale = max(previewSize.width / sourceSize.width, previewSize.height / sourceSize.height)
        val overflowX = (sourceSize.width * scale - previewSize.width) / 2f
        val overflowY = (sourceSize.height * scale - previewSize.height) / 2f
        val source = Rect(
            left = (visible.left + overflowX) / scale,
            top = (visible.top + overflowY) / scale,
            right = (visible.right + overflowX) / scale,
            bottom = (visible.bottom + overflowY) / scale,
        ).intersect(Rect(Offset.Zero, sourceSize))
        if (source.width <= 0f || source.height <= 0f) return Rect.Zero
        return Rect(
            left = source.left / sourceSize.width,
            top = source.top / sourceSize.height,
            right = source.right / sourceSize.width,
            bottom = source.bottom / sourceSize.height,
        )
    }

    fun layout(
        imageSize: Size,
        canvasFrame: Rect,
        cropScale: Float,
        cropMaximumSize: Float? = null,
        aspectRatio: Float,
    ): CropLayout {
        val safeImageWidth = max(imageSize.width, 1f)
        val safeImageHeight = max(imageSize.height, 1f)
        val safeAspect = max(aspectRatio, 0.01f)
        val safeCropScale = cropScale.coerceIn(0.1f, 1f)
        val cropWidth = min(
            min(canvasFrame.width * safeCropScale, canvasFrame.height * safeCropScale * safeAspect),
            cropMaximumSize?.coerceAtLeast(1f) ?: Float.MAX_VALUE,
        )
        val cropHeight = cropWidth / safeAspect
        val cropRect = Rect(
            left = canvasFrame.center.x - cropWidth / 2f,
            top = canvasFrame.center.y - cropHeight / 2f,
            right = canvasFrame.center.x + cropWidth / 2f,
            bottom = canvasFrame.center.y + cropHeight / 2f,
        )
        val baseScale = min(canvasFrame.width / safeImageWidth, canvasFrame.height / safeImageHeight)
        val minimumZoom = max(
            cropRect.width / max(safeImageWidth * baseScale, 1f),
            cropRect.height / max(safeImageHeight * baseScale, 1f),
        )
        return CropLayout(canvasFrame, cropRect, baseScale, minimumZoom)
    }

    fun displayedImageRect(
        imageSize: Size,
        canvasFrame: Rect,
        baseScale: Float,
        zoom: Float,
        offset: Offset,
    ): Rect {
        val width = max(imageSize.width * baseScale * zoom, 1f)
        val height = max(imageSize.height * baseScale * zoom, 1f)
        val center = canvasFrame.center + offset
        return Rect(
            left = center.x - width / 2f,
            top = center.y - height / 2f,
            right = center.x + width / 2f,
            bottom = center.y + height / 2f,
        )
    }

    fun clampedOffset(
        proposed: Offset,
        imageRect: Rect,
        cropRect: Rect,
    ): Offset {
        val baseCenter = imageRect.center - proposed
        val centerX = imageRect.center.x.coerceIn(
            cropRect.right - imageRect.width / 2f,
            cropRect.left + imageRect.width / 2f,
        )
        val centerY = imageRect.center.y.coerceIn(
            cropRect.bottom - imageRect.height / 2f,
            cropRect.top + imageRect.height / 2f,
        )
        return Offset(centerX - baseCenter.x, centerY - baseCenter.y)
    }

    fun normalizedCropRect(cropRect: Rect, imageRect: Rect): Rect {
        if (imageRect.width <= 0f || imageRect.height <= 0f) return Rect.Zero
        return Rect(
            left = (cropRect.left - imageRect.left) / imageRect.width,
            top = (cropRect.top - imageRect.top) / imageRect.height,
            right = (cropRect.right - imageRect.left) / imageRect.width,
            bottom = (cropRect.bottom - imageRect.top) / imageRect.height,
        ).intersect(Rect(0f, 0f, 1f, 1f))
    }
}
