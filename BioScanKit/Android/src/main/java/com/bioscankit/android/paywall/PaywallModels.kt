package com.bioscankit.android.paywall

import androidx.compose.runtime.Immutable
import com.bioscankit.android.design.BioScanTheme

enum class PaywallStyle { INature, CardSelection }
enum class PaywallProductKind { Lifetime, Consumable }

@Immutable
data class PaywallProduct(
    val id: String,
    val kind: PaywallProductKind,
    val title: String,
    val subtitle: String,
    val creditCount: Int? = null,
)

@Immutable
data class BillingProduct(
    val id: String,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val priceAmountMicros: Long? = null,
    val currencyCode: String? = null,
)

@Immutable
data class PurchaseCatalog(
    val lifetime: PaywallProduct,
    val consumables: List<PaywallProduct>,
)

@Immutable
data class PaywallCopy(
    val heroTitle: String,
    val heroSubtitle: String,
    val heroFootnote: String,
    val restoreTitle: String = "RESTORE",
    val retryTitle: String = "Retry",
    val connectingMessage: String = "Connecting to Google Play...",
)

@Immutable
data class PaywallConfiguration(
    val theme: BioScanTheme = BioScanTheme.INature,
    val style: PaywallStyle = PaywallStyle.INature,
    val catalog: PurchaseCatalog,
    val copy: PaywallCopy,
)

sealed interface EntitlementState {
    data object Unknown : EntitlementState
    data object Free : EntitlementState
    data object Lifetime : EntitlementState
}

sealed interface PurchaseResult {
    data class Success(val transactionId: String?) : PurchaseResult
    data object Cancelled : PurchaseResult
    data class Pending(val transactionId: String?) : PurchaseResult
    data class Error(val message: String) : PurchaseResult
}

@Immutable
data class CreditBalance(
    val free: Int,
    val purchased: Int,
) {
    val total: Int get() = free + purchased
}

enum class BillingOperation { Loading, Purchasing, Restoring }

sealed interface PaywallEvent {
    data object Shown : PaywallEvent
    data class ProductSelected(val productId: String) : PaywallEvent
    data class PurchaseFinished(val productId: String, val result: PurchaseResult) : PaywallEvent
    data class RestoreFinished(val result: PurchaseResult) : PaywallEvent
}

data class PaywallActions(
    val onDismiss: () -> Unit,
    val onReloadProducts: () -> Unit,
    val onPurchase: (String) -> Unit,
    val onRestore: () -> Unit,
    val track: (PaywallEvent) -> Unit = {},
)
