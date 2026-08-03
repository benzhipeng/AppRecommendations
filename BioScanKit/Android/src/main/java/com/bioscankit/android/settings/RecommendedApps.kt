package com.bioscankit.android.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bioscankit.android.design.BioScanDesign
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Immutable
data class RecommendedApp(
    val id: String,
    val title: String,
    val description: String,
    val destinationUrl: String,
    val imageName: String?,
    val backgroundColors: List<Color>,
    val fallbackMessage: String?,
)

object RecommendedAppsLoader {
    /**
     * Decodes the shared AppRecommendations document without assuming that an
     * iOS App Store URL is valid on Android. Hosts select their platform key.
     */
    fun load(
        json: String,
        excludingCurrentAppId: String,
        destinationUrlKey: String,
    ): List<RecommendedApp> = runCatching {
        val root = Json.parseToJsonElement(json).jsonObject
        root["apps"]?.jsonArray.orEmpty().mapNotNull { element ->
            val item = element.jsonObject
            val id = item["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val visible = item["isVisible"]?.jsonPrimitive?.booleanOrNull ?: true
            if (!visible || id.equals(excludingCurrentAppId, ignoreCase = true)) return@mapNotNull null
            val destination = item[destinationUrlKey]?.jsonPrimitive?.contentOrNull
                ?.takeIf { it.startsWith("https://") }
                ?: return@mapNotNull null
            RecommendedApp(
                id = id,
                title = item["title"]?.jsonPrimitive?.contentOrNull ?: id,
                description = item["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                destinationUrl = destination,
                imageName = item["imageName"]?.jsonPrimitive?.contentOrNull,
                backgroundColors = item["backgroundColors"]?.jsonArray.orEmpty().mapNotNull {
                    parseHexColor(it.jsonPrimitive.contentOrNull)
                },
                fallbackMessage = item["fallbackMessage"]?.jsonPrimitive?.contentOrNull,
            )
        }
    }.getOrDefault(emptyList())

    private fun parseHexColor(value: String?): Color? {
        val hex = value?.removePrefix("#") ?: return null
        if (hex.length != 6) return null
        return hex.toLongOrNull(16)?.let { Color(0xFF000000 or it) }
    }
}

@Composable
fun SettingsRecommendedAppCard(
    app: RecommendedApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable BoxScope.() -> Unit,
) {
    val colors = BioScanDesign.colors
    val gradient = app.backgroundColors.ifEmpty {
        listOf(colors.accent, colors.success)
    }
    androidx.compose.material3.Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .background(Brush.linearGradient(gradient))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
                content = icon,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    app.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                )
                Text(
                    app.description,
                    color = Color.White.copy(alpha = 0.90f),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                )
            }
            Text("↗", color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}
