import XCTest
@testable import BioScanPaywall

@MainActor
final class CreditLedgerTests: XCTestCase {
    private var suiteName: String!
    private var defaults: UserDefaults!

    override func setUp() {
        super.setUp()
        suiteName = "BioScanKitTests.\(UUID().uuidString)"
        defaults = UserDefaults(suiteName: suiteName)
    }

    override func tearDown() {
        defaults.removePersistentDomain(forName: suiteName)
        defaults = nil
        suiteName = nil
        super.tearDown()
    }

    func testRemainingCreditsAreSeededAndConsumed() {
        let ledger = CreditLedger(
            configuration: configuration(
                freeCredits: .remaining(key: "free", initialAllowance: 5)
            ),
            defaults: defaults
        )

        XCTAssertEqual(ledger.balance.free, 5)
        XCTAssertTrue(ledger.consumeOneCredit())
        XCTAssertEqual(ledger.balance.free, 4)
    }

    func testUsedCountStoragePreservesINatureSemantics() {
        defaults.set(2, forKey: "used")
        let ledger = CreditLedger(
            configuration: configuration(
                freeCredits: .used(key: "used", allowance: 5)
            ),
            defaults: defaults
        )

        XCTAssertEqual(ledger.balance.free, 3)
        XCTAssertTrue(ledger.consumeOneCredit())
        XCTAssertEqual(defaults.integer(forKey: "used"), 3)
    }

    func testTransactionIDPreventsDuplicateCredit() {
        let ledger = CreditLedger(
            configuration: configuration(
                freeCredits: .remaining(key: "free", initialAllowance: 0)
            ),
            defaults: defaults
        )

        XCTAssertTrue(ledger.addPurchasedCredits(5, transactionID: "transaction-1"))
        XCTAssertFalse(ledger.addPurchasedCredits(5, transactionID: "transaction-1"))
        XCTAssertEqual(ledger.balance.paid, 5)
    }

    func testLifetimeStringCache() {
        let ledger = CreditLedger(
            configuration: configuration(
                freeCredits: .remaining(key: "free", initialAllowance: 0)
            ),
            defaults: defaults
        )

        ledger.setLifetime(true)
        XCTAssertTrue(ledger.balance.hasUnlimitedAccess)
        XCTAssertEqual(defaults.string(forKey: "membership"), "lifetime")
    }

    private func configuration(
        freeCredits: FreeCreditStorage
    ) -> CreditStorageConfiguration {
        CreditStorageConfiguration(
            freeCredits: freeCredits,
            paidCreditsKey: "paid",
            lifetimeCache: .string(key: "membership", lifetimeValue: "lifetime"),
            didSeedFreeCreditsKey: "seeded",
            processedTransactionIDsKey: "transactions"
        )
    }
}
