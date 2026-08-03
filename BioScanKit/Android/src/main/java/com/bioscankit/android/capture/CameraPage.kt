package com.bioscankit.android.capture

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class CameraPageActions(
    val close: () -> Unit,
    val showHelp: (() -> Unit)? = null,
    val choosePhoto: () -> Unit,
    val capture: () -> Unit,
    val focusFrameChanged: (Rect) -> Unit = {},
)

/**
 * Native Compose camera page skeleton matching BioScanKit's iNature hierarchy.
 * CameraX ownership and gesture handling stay in the host app through slots.
 */
@Composable
fun CameraPage(
    configuration: CameraScreenConfiguration = CameraScreenConfiguration.INature,
    modifier: Modifier = Modifier,
    preview: @Composable BoxScope.() -> Unit,
    topBar: @Composable ColumnScope.() -> Unit,
    bottomBar: @Composable ColumnScope.() -> Unit,
    previewOverlay: @Composable BoxScope.() -> Unit = {},
    extraOverlay: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize().background(Color.Black),
    ) {
        preview()
        previewOverlay()
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.04f),
                            Color.Black.copy(alpha = 0.54f),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
            ) {
                topBar()
                Spacer(Modifier.weight(1f))
                CameraInstructionPill(
                    text = configuration.instruction,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(22.dp))
                CameraFinder(
                    configuration = configuration,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.weight(1f))
                bottomBar()
            }
        }
        extraOverlay()
    }
}

@Composable
fun CameraInstructionPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.45f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White.copy(alpha = 0.90f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun CameraFinder(
    configuration: CameraScreenConfiguration = CameraScreenConfiguration.INature,
    modifier: Modifier = Modifier,
    onBoundsChanged: (Rect) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.78f)
            .widthIn(max = configuration.finderMaximumSize)
            .aspectRatio(1f)
            .onGloballyPositioned { onBoundsChanged(it.boundsInRoot()) }
            .testTag("recognition-guide-frame"),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 7.dp.toPx()
            val edge = size.minDimension * 0.18f
            val color = configuration.finderColor
            drawLine(color, Offset.Zero, Offset(edge, 0f), strokeWidth = stroke)
            drawLine(color, Offset.Zero, Offset(0f, edge), strokeWidth = stroke)
            drawLine(color, Offset(size.width, 0f), Offset(size.width - edge, 0f), strokeWidth = stroke)
            drawLine(color, Offset(size.width, 0f), Offset(size.width, edge), strokeWidth = stroke)
            drawLine(color, Offset(0f, size.height), Offset(edge, size.height), strokeWidth = stroke)
            drawLine(color, Offset(0f, size.height), Offset(0f, size.height - edge), strokeWidth = stroke)
            drawLine(color, Offset(size.width, size.height), Offset(size.width - edge, size.height), strokeWidth = stroke)
            drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - edge), strokeWidth = stroke)
            drawRect(Color.White.copy(alpha = 0.14f), style = Stroke(width = 1.dp.toPx()))
        }
    }
}
