import BioScanDesign
import SwiftUI

public struct PaywallScreen<Hero: View>: View {
    @ObservedObject private var store: PaywallStore
    private let configuration: PaywallConfiguration
    private let hero: Hero

    public init(
        configuration: PaywallConfiguration,
        store: PaywallStore,
        @ViewBuilder hero: () -> Hero
    ) {
        self.configuration = configuration
        self.store = store
        self.hero = hero()
    }

    public var body: some View {
        Group {
            switch configuration.style {
            case .iNature:
                INaturePaywallView(
                    configuration: configuration,
                    store: store,
                    hero: hero
                )
            case .cardSelection:
                CardSelectionPaywallView(
                    configuration: configuration,
                    store: store,
                    hero: hero
                )
            }
        }
        .alert(
            store.notice?.title ?? "",
            isPresented: Binding(
                get: { store.notice != nil },
                set: { isPresented in
                    if !isPresented {
                        store.clearNotice()
                    }
                }
            )
        ) {
            Button("OK", role: .cancel) {
                store.clearNotice()
            }
        } message: {
            Text(store.notice?.message ?? "")
        }
        .task {
            store.appeared()
            await store.load()
        }
        .overlay {
            if let coordinator = store.recoveryCoordinator {
                PurchaseRecoveryOverlay(
                    coordinator: coordinator,
                    theme: configuration.theme
                )
            }
        }
        .onChange(of: store.recoveryCoordinator?.isOfferPresented) { oldValue, newValue in
            guard oldValue == true, newValue == false,
                  let coordinator = store.recoveryCoordinator else { return }
            store.refreshCreditBalance()
            if coordinator.currentRecord.state == .claimed {
                store.dismiss()
            }
        }
    }
}

private struct PurchaseRecoveryOverlay: View {
    @ObservedObject var coordinator: PurchaseRecoveryCoordinator
    let theme: BioScanTheme

    var body: some View {
        if coordinator.isOfferPresented {
            ZStack {
                Color.black.opacity(0.42)
                    .ignoresSafeArea()

                PurchaseRecoverySheet(
                    coordinator: coordinator,
                    theme: theme
                )
                .padding(24)
            }
            .zIndex(1)
        }
    }
}

public extension PaywallScreen where Hero == EmptyView {
    init(
        configuration: PaywallConfiguration,
        store: PaywallStore
    ) {
        self.init(configuration: configuration, store: store) {
            EmptyView()
        }
    }
}
