package com.bioscankit.android.capture

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Test

class CropGeometryTest {
    @Test
    fun aspectFillMappingAccountsForHorizontalOverflow() {
        val normalized = CropGeometry.normalizedSourceRectForAspectFill(
            previewRect = Rect(0f, 0f, 300f, 300f),
            previewSize = Size(300f, 300f),
            sourceSize = Size(400f, 200f),
        )

        assertEquals(0.25f, normalized.left, 0.0001f)
        assertEquals(0f, normalized.top, 0.0001f)
        assertEquals(0.75f, normalized.right, 0.0001f)
        assertEquals(1f, normalized.bottom, 0.0001f)
    }

    @Test
    fun layoutKeepsCropInsideCanvasAndComputesMinimumZoom() {
        val layout = CropGeometry.layout(
            imageSize = Size(400f, 200f),
            canvasFrame = Rect(0f, 0f, 300f, 600f),
            cropScale = 0.78f,
            aspectRatio = 1f,
        )

        assertEquals(234f, layout.cropRect.width, 0.001f)
        assertEquals(1.56f, layout.minimumZoom, 0.001f)
    }
}
