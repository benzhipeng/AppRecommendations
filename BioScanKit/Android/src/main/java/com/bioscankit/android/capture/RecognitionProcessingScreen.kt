package com.bioscankit.android.capture

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bioscankit.android.design.resolve
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun RecognitionProcessingScreen(
    preview: ImageBitmap?,
    modifier: Modifier = Modifier,
    configuration: ProcessingScreenConfiguration = ProcessingScreenConfiguration.INature,
    animationsEnabled: Boolean = true,
    fallback: @Composable BoxScope.() -> Unit = {},
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val colors = configuration.theme.resolve(isDark)
    var statusIndex by remember(configuration, animationsEnabled) {
        mutableIntStateOf(if (animationsEnabled) 0 else configuration.statuses.lastIndex)
    }
    var progressTarget by remember(configuration, animationsEnabled) {
        mutableFloatStateOf(if (animationsEnabled) 0.08f else configuration.progressTargets.last())
    }
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 720, easing = FastOutSlowInEasing),
        label = "recognition-progress",
    )
    val transition = rememberInfiniteTransition(label = "recognition-ambient")
    val beamProgress by transition.animateFloat(
        initialValue = if (animationsEnabled) -0.18f else 0.48f,
        targetValue = if (animationsEnabled) 1.18f else 0.48f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "recognition-scan-beam",
    )
    val glowScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = if (animationsEnabled) 1.06f else 0.96f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "recognition-ambient-glow",
    )
    val pulseScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = if (animationsEnabled) 1.32f else 0.92f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "recognition-waiting-pulse",
    )

    LaunchedEffect(configuration, animationsEnabled) {
        if (!animationsEnabled) return@LaunchedEffect
        configuration.progressTargets.forEachIndexed { index, target ->
            statusIndex = index
            progressTarget = target
            delay(if (index == 0) 420 else 780)
        }
    }

    val accent = if (isDark) Color(0xFF70D879) else colors.accent
    val background = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF111A15), Color(0xFF14271B), Color(0xFF0B100D)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFEEF3EC), Color(0xFFE2EEE1), Color(0xFFF5F7F4)))
    }
    val cardSurface = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.78f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .testTag("recognition-processing-screen"),
    ) {
        val horizontalPadding = 22.dp
        val contentWidth = (maxWidth - horizontalPadding * 2).coerceAtMost(390.dp)
        val scanCardHeight = (contentWidth * 1.08f).coerceAtLeast(330.dp).coerceAtMost(maxHeight * 0.54f)
        val markerSize = (contentWidth - 46.dp).coerceAtMost(scanCardHeight - 58.dp)

        Canvas(Modifier.fillMaxSize()) {
            val spacing = size.minDimension / 11f
            var y = -size.width * 0.15f
            while (y < size.height + size.width * 0.15f) {
                drawLine(
                    color = accent.copy(alpha = if (isDark) 0.035f else 0.055f),
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y + size.width * 0.18f),
                    strokeWidth = 1f,
                )
                y += spacing
            }
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(accent.copy(alpha = if (isDark) 0.16f else 0.24f), Color.Transparent),
                    center,
                    size.minDimension * 0.52f * glowScale,
                ),
                radius = size.minDimension * 0.52f * glowScale,
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.heightIn(min = 24.dp).weight(1.15f))
            Column(Modifier.width(contentWidth), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    configuration.eyebrow,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.4.sp,
                    ),
                    color = accent,
                )
                Text(
                    configuration.statuses[statusIndex],
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Black),
                    color = colors.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Box(
                modifier = Modifier
                    .width(contentWidth)
                    .height(scanCardHeight)
                    .shadow(18.dp, RoundedCornerShape(30.dp), ambientColor = accent.copy(alpha = 0.18f))
                    .background(cardSurface, RoundedCornerShape(30.dp))
                    .border(1.2.dp, accent.copy(alpha = 0.24f), RoundedCornerShape(30.dp))
                    .padding(11.dp),
            ) {
                BoxWithConstraints(Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))) {
                    val cardHeightPx = constraints.maxHeight
                    if (preview != null) {
                        Image(
                            bitmap = preview,
                            contentDescription = "Photo being analyzed",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(accent.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                            content = fallback,
                        )
                    }
                    Box(Modifier.fillMaxSize().background((if (isDark) Color.Black else Color.White).copy(alpha = 0.08f)))
                    if (animationsEnabled) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(88.dp)
                                .offset { IntOffset(0, ((cardHeightPx + 176) * beamProgress - 88).roundToInt()) }
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            accent.copy(alpha = 0.10f),
                                            accent.copy(alpha = 0.50f),
                                            accent.copy(alpha = 0.10f),
                                            Color.Transparent,
                                        ),
                                    ),
                                ),
                        )
                    }
                }
                ProcessingCornerMarkers(accent.copy(alpha = 0.82f), Modifier.align(Alignment.Center).size(markerSize))
                if (statusIndex == configuration.statuses.lastIndex) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                            .background(cardSurface, CircleShape)
                            .padding(horizontal = 11.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                                .background(accent.copy(alpha = 0.90f), CircleShape),
                        )
                        Text(
                            configuration.waitingText,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp, fontWeight = FontWeight.Black),
                            color = colors.primaryText.copy(alpha = 0.82f),
                        )
                    }
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.width(contentWidth).height(4.dp).padding(horizontal = 4.dp).clip(CircleShape),
                color = accent.copy(alpha = 0.92f),
                trackColor = colors.secondaryText.copy(alpha = 0.18f),
            )
            Spacer(Modifier.heightIn(min = 24.dp).weight(0.85f))
        }
    }
}

@Composable
private fun ProcessingCornerMarkers(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val length = size.width * 0.12f
        val stroke = 3.dp.toPx()
        val w = size.width
        val h = size.height
        drawLine(color, androidx.compose.ui.geometry.Offset(0f, length), androidx.compose.ui.geometry.Offset.Zero, stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset.Zero, androidx.compose.ui.geometry.Offset(length, 0f), stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(w - length, 0f), androidx.compose.ui.geometry.Offset(w, 0f), stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(w, 0f), androidx.compose.ui.geometry.Offset(w, length), stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(0f, h - length), androidx.compose.ui.geometry.Offset(0f, h), stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(0f, h), androidx.compose.ui.geometry.Offset(length, h), stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(w, h - length), androidx.compose.ui.geometry.Offset(w, h), stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(w, h), androidx.compose.ui.geometry.Offset(w - length, h), stroke)
    }
}
