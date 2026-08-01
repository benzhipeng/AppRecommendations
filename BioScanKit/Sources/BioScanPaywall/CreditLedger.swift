import Foundation

public enum FreeCreditStorage: Sendable {
    case remaining(key: String, initialAllowance: Int)
    case used(key: String, allowance: Int)
}

public enum LifetimeCacheStorage: Sendable {
    case boolean(key: String)
    case string(key: String, lifetimeValue: String)
}

public struct CreditStorageConfiguration: Sendable {
    public let freeCredits: FreeCreditStorage
    public let paidCreditsKey: String
    public let lifetimeCache: LifetimeCacheStorage
    public let didSeedFreeCreditsKey: String?
    public let processedTransactionIDsKey: String

    public init(
        freeCredits: FreeCreditStorage,
        paidCreditsKey: String,
        lifetimeCache: LifetimeCacheStorage,
        didSeedFreeCreditsKey: String? = nil,
        processedTransactionIDsKey: String
    ) {
        self.freeCredits = freeCredits
        self.paidCreditsKey = paidCreditsKey
        self.lifetimeCache = lifetimeCache
        self.didSeedFreeCreditsKey = didSeedFreeCreditsKey
        self.processedTransactionIDsKey = processedTransactionIDsKey
    }
}

@MainActor
public final class CreditLedger {
    private let defaults: UserDefaults
    private let configuration: CreditStorageConfiguration

    public init(
        configuration: CreditStorageConfiguration,
        defaults: UserDefaults = .standard
    ) {
        self.configuration = configuration
        self.defaults = defaults
        seedFreeCreditsIfNeeded()
    }

    public var balance: CreditBalance {
        CreditBalance(
            free: freeCredits,
            paid: max(0, defaults.integer(forKey: configuration.paidCreditsKey)),
            hasUnlimitedAccess: cachedLifetime
        )
    }

    @discardableResult
    public func addPurchasedCredits(
        _ amount: Int,
        transactionID: String?
    ) -> Bool {
        guard amount > 0 else { return false }

        if let transactionID {
            var processed = Set(
                defaults.stringArray(
                    forKey: configuration.processedTransactionIDsKey
                ) ?? []
            )
            guard processed.insert(transactionID).inserted else { return false }
            defaults.set(
                Array(processed).sorted(),
                forKey: configuration.processedTransactionIDsKey
            )
        }

        let current = max(0, defaults.integer(forKey: configuration.paidCreditsKey))
        defaults.set(current + amount, forKey: configuration.paidCreditsKey)
        return true
    }

    public func consumeOneCredit() -> Bool {
        guard !cachedLifetime else { return true }

        if freeCredits > 0 {
            switch configuration.freeCredits {
            case .remaining(let key, _):
                defaults.set(max(0, defaults.integer(forKey: key) - 1), forKey: key)
            case .used(let key, let allowance):
                let used = min(max(defaults.integer(forKey: key), 0), allowance)
                defaults.set(used + 1, forKey: key)
            }
            return true
        }

        let paid = max(0, defaults.integer(forKey: configuration.paidCreditsKey))
        guard paid > 0 else { return false }
        defaults.set(paid - 1, forKey: configuration.paidCreditsKey)
        return true
    }

    public func setLifetime(_ isActive: Bool) {
        switch configuration.lifetimeCache {
        case .boolean(let key):
            defaults.set(isActive, forKey: key)
        case .string(let key, let lifetimeValue):
            if isActive {
                defaults.set(lifetimeValue, forKey: key)
            } else if defaults.string(forKey: key) == lifetimeValue {
                defaults.removeObject(forKey: key)
            }
        }
    }

    private var freeCredits: Int {
        switch configuration.freeCredits {
        case .remaining(let key, _):
            return max(0, defaults.integer(forKey: key))
        case .used(let key, let allowance):
            return max(0, allowance - max(0, defaults.integer(forKey: key)))
        }
    }

    private var cachedLifetime: Bool {
        switch configuration.lifetimeCache {
        case .boolean(let key):
            return defaults.bool(forKey: key)
        case .string(let key, let lifetimeValue):
            return defaults.string(forKey: key) == lifetimeValue
        }
    }

    private func seedFreeCreditsIfNeeded() {
        guard case .remaining(let key, let allowance) = configuration.freeCredits else {
            return
        }

        if let seedKey = configuration.didSeedFreeCreditsKey {
            guard !defaults.bool(forKey: seedKey) else { return }
            defaults.set(max(0, allowance), forKey: key)
            defaults.set(true, forKey: seedKey)
        } else if defaults.object(forKey: key) == nil {
            defaults.set(max(0, allowance), forKey: key)
        }
    }
}
