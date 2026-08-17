import XCTest
@testable import BioScanPaywall

final class BillingProductTests: XCTestCase {
    func testLocalizedOriginalPriceDoublesAppStorePrice() {
        let product = BillingProduct(
            id: "lifetime",
            localizedPrice: "$27.99",
            price: 27.99,
            currencyCode: "USD",
            localeIdentifier: "en_US"
        )

        XCTAssertEqual(product.localizedPrice(multipliedBy: 2), "$55.98")
    }

    func testLocalizedOriginalPriceUsesStoreLocale() {
        let product = BillingProduct(
            id: "lifetime",
            localizedPrice: "27,99 €",
            price: 27.99,
            currencyCode: "EUR",
            localeIdentifier: "de_DE"
        )

        XCTAssertEqual(product.localizedPrice(multipliedBy: 2), "55,98 €")
    }
}
