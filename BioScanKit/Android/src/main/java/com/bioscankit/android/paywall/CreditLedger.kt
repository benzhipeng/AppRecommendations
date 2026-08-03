package com.bioscankit.android.paywall

interface CreditStorage {
    fun readFreeCredits(): Int
    fun writeFreeCredits(value: Int)
    fun readPurchasedCredits(): Int
    fun writePurchasedCredits(value: Int)
    fun containsTransaction(transactionId: String): Boolean
    fun recordTransaction(transactionId: String)
}

sealed interface CreditDeliveryResult {
    data class Delivered(val balance: CreditBalance) : CreditDeliveryResult
    data class AlreadyDelivered(val balance: CreditBalance) : CreditDeliveryResult
}

class CreditLedger(
    private val storage: CreditStorage,
) {
    @Synchronized
    fun balance(): CreditBalance = CreditBalance(
        free = storage.readFreeCredits().coerceAtLeast(0),
        purchased = storage.readPurchasedCredits().coerceAtLeast(0),
    )

    @Synchronized
    fun deliverPurchasedCredits(
        transactionId: String,
        credits: Int,
    ): CreditDeliveryResult {
        require(transactionId.isNotBlank()) { "A transaction ID is required for idempotent delivery." }
        require(credits > 0) { "Credit delivery must be positive." }
        if (storage.containsTransaction(transactionId)) {
            return CreditDeliveryResult.AlreadyDelivered(balance())
        }
        storage.writePurchasedCredits(storage.readPurchasedCredits().coerceAtLeast(0) + credits)
        storage.recordTransaction(transactionId)
        return CreditDeliveryResult.Delivered(balance())
    }

    @Synchronized
    fun consumeOne(): Boolean {
        val purchased = storage.readPurchasedCredits().coerceAtLeast(0)
        if (purchased > 0) {
            storage.writePurchasedCredits(purchased - 1)
            return true
        }
        val free = storage.readFreeCredits().coerceAtLeast(0)
        if (free > 0) {
            storage.writeFreeCredits(free - 1)
            return true
        }
        return false
    }
}
