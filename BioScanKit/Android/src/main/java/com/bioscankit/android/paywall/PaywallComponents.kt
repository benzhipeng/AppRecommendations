package com.bioscankit.android.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bioscankit.android.design.BioScanDesign

@Composable
fun PaywallTopBar(
    restoreTitle: String,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
    closeIcon: @Composable () -> Unit,
) {
    val colors = BioScanDesign.colors
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(34.dp).clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            closeIcon()
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = restoreTitle,
            color = colors.accent,
            fontSize = 14.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .clickable(enabled = !isBusy, onClick = onRestore)
                .padding(horizontal = 4.dp, vertical = 8.dp),
        )
    }
}

@Composable
fun PaywallStoreStatusBanner(
    isLoading: Boolean,
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    statusIcon: @Composable () -> Unit,
) {
    if (message.isNullOrBlank() && !isLoading) return
    val colors = BioScanDesign.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.cardBackground, RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = colors.accent,
            )
        } else {
            statusIcon()
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = if (isLoading) "Connecting to Google Play..." else message.orEmpty(),
            color = colors.primaryText,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f),
        )
        if (!isLoading) {
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Retry",
                color = colors.accent,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.clickable(onClick = onRetry).padding(vertical = 6.dp),
            )
        }
    }
}

@Composable
fun PaywallHero(
    copy: PaywallCopy,
    modifier: Modifier = Modifier,
) {
    val colors = BioScanDesign.colors
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.cardBackground, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = copy.heroTitle,
            color = colors.primaryText,
            fontSize = 30.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = copy.heroSubtitle,
            color = colors.accent,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = copy.heroFootnote,
            color = colors.success,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Black,
        )
    }
}
