package com.bioscankit.android.paywall

interface BillingClient {
    suspend fun loadProducts(productIds: List<String>): List<BillingProduct>
    suspend fun purchase(productId: String): PurchaseResult
    suspend fun restore(): PurchaseResult
    suspend fun entitlementState(): EntitlementState
}

class ClosureBillingClient(
    private val loadProductsBlock: suspend (List<String>) -> List<BillingProduct>,
    private val purchaseBlock: suspend (String) -> PurchaseResult,
    private val restoreBlock: suspend () -> PurchaseResult,
    private val entitlementBlock: suspend () -> EntitlementState,
) : BillingClient {
    override suspend fun loadProducts(productIds: List<String>): List<BillingProduct> =
        loadProductsBlock(productIds)

    override suspend fun purchase(productId: String): PurchaseResult = purchaseBlock(productId)

    override suspend fun restore(): PurchaseResult = restoreBlock()

    override suspend fun entitlementState(): EntitlementState = entitlementBlock()
}
