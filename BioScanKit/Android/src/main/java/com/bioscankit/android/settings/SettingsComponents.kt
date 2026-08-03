package com.bioscankit.android.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bioscankit.android.design.BioScanDesign

@Composable
fun SettingsPageContainer(
    versionText: String,
    modifier: Modifier = Modifier,
    onVersionTapped: (() -> Unit)? = null,
    content: @Composable ColumnScope.(SettingsPageMetrics) -> Unit,
) {
    val colors = BioScanDesign.colors
    val systemBackground = if (BioScanDesign.isDark) Color.Black else Color.White
    val secondarySystemBackground = if (BioScanDesign.isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(systemBackground, secondarySystemBackground),
                ),
            ),
    ) {
        val widthDp = maxWidth.value.toInt()
        val twoColumns = widthDp >= 960
        val metrics = SettingsPageMetrics(
            usesTwoColumnLayout = twoColumns,
            contentMaxWidthDp = if (twoColumns) 1080 else 760,
            horizontalPaddingDp = if (twoColumns) 24 else 18,
            columnSpacingDp = if (twoColumns) 22 else 0,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = metrics.horizontalPaddingDp.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = metrics.contentMaxWidthDp.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                content(metrics)
                Text(
                    text = versionText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.secondaryText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = onVersionTapped != null) { onVersionTapped?.invoke() }
                        .padding(top = 4.dp, bottom = 14.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun SettingsTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backContent: @Composable () -> Unit,
) {
    val colors = BioScanDesign.colors
    val navigationBackground = if (BioScanDesign.isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.88f)
    }
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Surface(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = onBack,
            shape = CircleShape,
            color = navigationBackground,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center,
            ) {
                backContent()
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsSectionTitle(title)
        SettingsCard(content = content)
    }
}

@Composable
fun SettingsSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
        ),
        color = BioScanDesign.colors.secondaryText,
        modifier = modifier,
    )
}

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = BioScanDesign.colors
    val secondarySystemBackground = if (BioScanDesign.isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(secondarySystemBackground, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String? = null,
    detail: String? = null,
    accessory: SettingsAccessory = SettingsAccessory.None,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    leading: @Composable () -> Unit,
) {
    val colors = BioScanDesign.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.5f),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = if (subtitle == null) Alignment.CenterVertically else Alignment.Top,
    ) {
        SettingsIconTile(content = leading)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primaryText,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.secondaryText,
                )
            }
        }
        detail?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.secondaryText,
            )
        }
        when (accessory) {
            SettingsAccessory.None -> Unit
            SettingsAccessory.Navigation,
            SettingsAccessory.External,
            -> Text(
                text = if (accessory == SettingsAccessory.Navigation) "›" else "↗",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.secondaryText,
                modifier = Modifier.padding(top = if (subtitle == null) 2.dp else 6.dp),
            )
        }
    }
}

@Composable
fun SettingsIconTile(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = BioScanDesign.colors
    val systemBackground = if (BioScanDesign.isDark) Color.Black else Color.White
    Box(
        modifier = modifier
            .size(44.dp)
            .background(systemBackground, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun SettingsDangerRow(
    title: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    leading: @Composable () -> Unit,
) {
    val colors = BioScanDesign.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.5f),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.danger,
        )
    }
}

@Composable
fun SettingsDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = BioScanDesign.colors.border.copy(alpha = 0.55f),
    )
}

@Composable
fun SettingsSegmentedControl(
    options: List<SettingsSegmentOption>,
    selectedId: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (options.isEmpty()) return
    val isDark = BioScanDesign.isDark
    val colors = BioScanDesign.colors
    val container = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE9E9EE)
    val selected = if (isDark) Color(0xFF4A4A4E) else Color.White
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(container)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { option ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (option.id == selectedId) selected else Color.Transparent)
                    .clickable { onSelected(option.id) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colors.primaryText,
                )
            }
        }
    }
}

@Composable
fun SettingsMembershipCard(
    state: SettingsMembershipState,
    modifier: Modifier = Modifier,
    isActionDisabled: Boolean = false,
    onAction: (() -> Unit)? = null,
    leading: @Composable () -> Unit = {},
) {
    val colors = BioScanDesign.colors
    val secondarySystemBackground = if (BioScanDesign.isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(secondarySystemBackground, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when (state) {
            is SettingsMembershipState.Lifetime -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leading()
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                        color = colors.primaryText,
                    )
                    Text(
                        text = state.subtitle,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.secondaryText,
                    )
                }
            }

            is SettingsMembershipState.Upgrade -> {
                state.remainingText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colors.primaryText,
                    )
                }
                Surface(
                    enabled = !isActionDisabled && onAction != null,
                    onClick = { onAction?.invoke() },
                    shape = RoundedCornerShape(999.dp),
                    color = colors.accent,
                    contentColor = Color.White,
                ) {
                    Text(
                        text = state.actionTitle,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                Text(
                    text = state.footnote,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = colors.accent,
                )
            }
        }
    }
}
