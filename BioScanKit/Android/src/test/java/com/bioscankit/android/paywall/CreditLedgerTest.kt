package com.bioscankit.android.paywall

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreditLedgerTest {
    @Test
    fun transactionDeliveryIsIdempotent() {
        val storage = MemoryCreditStorage()
        val ledger = CreditLedger(storage)

        assertTrue(ledger.deliverPurchasedCredits("order-1", 5) is CreditDeliveryResult.Delivered)
        assertTrue(ledger.deliverPurchasedCredits("order-1", 5) is CreditDeliveryResult.AlreadyDelivered)
        assertEquals(5, ledger.balance().purchased)
    }

    @Test
    fun purchasedCreditsAreConsumedBeforeFreeCredits() {
        val storage = MemoryCreditStorage(free = 2, purchased = 1)
        val ledger = CreditLedger(storage)

        assertTrue(ledger.consumeOne())
        assertEquals(CreditBalance(free = 2, purchased = 0), ledger.balance())
        assertTrue(ledger.consumeOne())
        assertEquals(CreditBalance(free = 1, purchased = 0), ledger.balance())
        assertTrue(ledger.consumeOne())
        assertFalse(ledger.consumeOne())
    }
}

private class MemoryCreditStorage(
    private var free: Int = 0,
    private var purchased: Int = 0,
) : CreditStorage {
    private val transactions = mutableSetOf<String>()

    override fun readFreeCredits(): Int = free
    override fun writeFreeCredits(value: Int) { free = value }
    override fun readPurchasedCredits(): Int = purchased
    override fun writePurchasedCredits(value: Int) { purchased = value }
    override fun containsTransaction(transactionId: String): Boolean = transactionId in transactions
    override fun recordTransaction(transactionId: String) { transactions += transactionId }
}
