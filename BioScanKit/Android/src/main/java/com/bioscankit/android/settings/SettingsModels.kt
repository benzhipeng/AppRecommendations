package com.bioscankit.android.settings

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsPageMetrics(
    val usesTwoColumnLayout: Boolean,
    val contentMaxWidthDp: Int,
    val horizontalPaddingDp: Int,
    val columnSpacingDp: Int,
)

enum class SettingsAccessory {
    None,
    Navigation,
    External,
}

@Immutable
data class SettingsSegmentOption(
    val id: String,
    val title: String,
)

sealed interface SettingsMembershipState {
    data class Lifetime(
        val title: String,
        val subtitle: String,
    ) : SettingsMembershipState

    data class Upgrade(
        val remainingText: String?,
        val actionTitle: String,
        val footnote: String,
    ) : SettingsMembershipState
}

sealed interface RestoreResult {
    data object Success : RestoreResult
    data object NotFound : RestoreResult
    data class Error(val message: String) : RestoreResult
}

sealed interface SettingsEvent {
    data class RowTapped(val id: String) : SettingsEvent
    data object RestoreTapped : SettingsEvent
    data object VersionTapped : SettingsEvent
}

data class SettingsActions(
    val onRestore: () -> RestoreResult = { RestoreResult.NotFound },
    val onOpenSystemSettings: () -> Unit = {},
    val onOpenPrivacyPolicy: () -> Unit = {},
    val onOpenUserAgreement: () -> Unit = {},
    val onSendFeedback: () -> Unit = {},
    val onRateApp: () -> Unit = {},
    val track: (SettingsEvent) -> Unit = {},
)
