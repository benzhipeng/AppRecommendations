package com.bioscankit.android.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AdaptiveColor(
    val light: Color,
    val dark: Color,
) {
    fun resolve(isDark: Boolean): Color = if (isDark) dark else light
}

@Immutable
data class BioScanTheme(
    val accent: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val pageBackground: AdaptiveColor,
    val cardBackground: AdaptiveColor,
    val elevatedCardBackground: AdaptiveColor,
    val primaryText: AdaptiveColor,
    val secondaryText: AdaptiveColor,
    val border: AdaptiveColor,
    val cardCornerRadius: Dp = 18.dp,
    val buttonCornerRadius: Dp = 14.dp,
    val horizontalPadding: Dp = 18.dp,
    val sectionSpacing: Dp = 20.dp,
) {
    companion object {
        /** Canonical visual configuration, matched to BioScanKit's iNature theme. */
        val INature = BioScanTheme(
            accent = Color(0xFF248533),
            success = Color(0xFF299E54),
            warning = Color(0xFFED941F),
            danger = Color(0xFFD74747),
            pageBackground = AdaptiveColor(
                light = Color(0xFFF2F5F2),
                dark = Color(0xFF14191F),
            ),
            cardBackground = AdaptiveColor(
                light = Color.White,
                dark = Color(0xFF21262E),
            ),
            elevatedCardBackground = AdaptiveColor(
                light = Color(0xFFE3EDE3),
                dark = Color(0xFF263129),
            ),
            primaryText = AdaptiveColor(
                light = Color(0xFF141F33),
                dark = Color(0xFFEDF2FA),
            ),
            secondaryText = AdaptiveColor(
                light = Color(0xFF8594A8),
                dark = Color(0xFF9EADC2),
            ),
            border = AdaptiveColor(
                light = Color(0xFFD6E0E8),
                dark = Color.White.copy(alpha = 0.14f),
            ),
        )
    }
}

@Immutable
data class ResolvedBioScanColors(
    val accent: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val pageBackground: Color,
    val cardBackground: Color,
    val elevatedCardBackground: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val border: Color,
)

private val LocalBioScanTheme = staticCompositionLocalOf { BioScanTheme.INature }
private val LocalBioScanIsDark = staticCompositionLocalOf<Boolean?> { null }

object BioScanDesign {
    val theme: BioScanTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalBioScanTheme.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalBioScanIsDark.current ?: isSystemInDarkTheme()

    val colors: ResolvedBioScanColors
        @Composable
        @ReadOnlyComposable
        get() = theme.resolve(isDark)
}

fun BioScanTheme.resolve(isDark: Boolean): ResolvedBioScanColors = ResolvedBioScanColors(
    accent = accent,
    success = success,
    warning = warning,
    danger = danger,
    pageBackground = pageBackground.resolve(isDark),
    cardBackground = cardBackground.resolve(isDark),
    elevatedCardBackground = elevatedCardBackground.resolve(isDark),
    primaryText = primaryText.resolve(isDark),
    secondaryText = secondaryText.resolve(isDark),
    border = border.resolve(isDark),
)

@Composable
fun BioScanDesignSystem(
    theme: BioScanTheme = BioScanTheme.INature,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalBioScanTheme provides theme,
        LocalBioScanIsDark provides darkTheme,
    ) {
        MaterialTheme(content = content)
    }
}
