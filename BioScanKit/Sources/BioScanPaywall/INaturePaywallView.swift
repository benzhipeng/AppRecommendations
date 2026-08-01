import SwiftUI

struct LegacyINaturePaywallView<Hero: View>: View {
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.scenePhase) private var scenePhase
    @ObservedObject var store: PaywallStore

    let configuration: PaywallConfiguration
    let hero: Hero

    init(
        configuration: PaywallConfiguration,
        store: PaywallStore,
        hero: Hero
    ) {
        self.configuration = configuration
        self.store = store
        self.hero = hero
    }

    var body: some View {
        GeometryReader { proxy in
            let metrics = LayoutMetrics(width: proxy.size.width)
            let bottomInset = max(proxy.safeAreaInsets.bottom, 8)
            let bottomReservedSpace = bottomInset + 96

            ZStack(alignment: .bottom) {
                pageBackground
                    .ignoresSafeArea()

                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: metrics.sectionSpacing) {
                        topBar
                        statusBanner
                        heroSection
                        lifetimeCard
                        creditSection
                    }
                    .frame(maxWidth: metrics.contentMaxWidth)
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal, metrics.horizontalPadding)
                    .padding(.top, 18)
                    .padding(.bottom, bottomReservedSpace)
                }

                bottomCTA
                    .frame(maxWidth: metrics.contentMaxWidth)
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal, metrics.horizontalPadding)
                    .padding(.top, 8)
                    .padding(.bottom, bottomInset)
                    .background(
                        pageBackground
                            .opacity(0.98)
                            .ignoresSafeArea(edges: .bottom)
                    )
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .onChange(of: store.operation) { operation in
            guard operation == .succeeded else { return }
            store.dismiss()
        }
        .onChange(of: scenePhase) { phase in
            guard phase == .active,
                  store.products.isEmpty,
                  !store.isBusy else {
                return
            }
            Task {
                await store.load()
            }
        }
    }

    private var topBar: some View {
        HStack {
            Button(action: store.dismiss) {
                Image(systemName: "xmark")
                    .font(.system(size: 20, weight: .medium))
                    .foregroundStyle(secondaryText)
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Close")

            Spacer()

            Button(configuration.copy.restoreTitle) {
                Task {
                    await store.restorePurchases()
                }
            }
            .font(.system(size: 14, weight: .black, design: .rounded))
            .foregroundStyle(accent)
            .buttonStyle(.plain)
            .disabled(store.isBusy)
            .opacity(store.isBusy ? 0.6 : 1)
        }
        .padding(.horizontal, 4)
    }

    @ViewBuilder
    private var statusBanner: some View {
        switch store.operation {
        case .loadingProducts:
            statusBannerContent(
                message: configuration.copy.loadingStoreTitle,
                showsProgress: true,
                showsRetry: false
            )
        case .failed(let message):
            statusBannerContent(
                message: message,
                showsProgress: false,
                showsRetry: true
            )
        default:
            EmptyView()
        }
    }

    private func statusBannerContent(
        message: String,
        showsProgress: Bool,
        showsRetry: Bool
    ) -> some View {
        HStack(spacing: 10) {
            if showsProgress {
                ProgressView()
                    .controlSize(.small)
                    .tint(accent)
            } else {
                Image(systemName: "wifi.exclamationmark")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(configuration.theme.warning)
            }

            Text(message)
                .font(.system(size: 13, weight: .semibold, design: .rounded))
                .foregroundStyle(primaryText)

            Spacer()

            if showsRetry {
                Button("Retry") {
                    Task {
                        await store.load()
                    }
                }
                .font(.system(size: 13, weight: .bold, design: .rounded))
                .foregroundStyle(accent)
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .background(
            cardBackground,
            in: .rect(cornerRadius: 14)
        )
        .overlay {
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(cardBorder, lineWidth: 1)
        }
    }

    private var heroSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Your offline scanner")
                .font(.system(size: 30, weight: .black, design: .rounded))
                .foregroundStyle(primaryText)
                .lineLimit(2)

            Text("Perfect for hikes, camping, and travel without signal")
                .font(.system(size: 14, weight: .bold, design: .rounded))
                .foregroundStyle(accent)

            Text("All yours — no subscription hassle!")
                .font(.system(size: 18, weight: .black, design: .rounded))
                .foregroundStyle(Color(red: 0.11, green: 0.42, blue: 0.16))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(
            cardBackground,
            in: .rect(cornerRadius: 16)
        )
        .overlay {
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(cardBorder, lineWidth: 1)
        }
    }

    @ViewBuilder
    private var lifetimeCard: some View {
        if let product = lifetimeProduct {
            Button {
                store.select(product.id)
            } label: {
                VStack(alignment: .leading, spacing: 18) {
                    HStack(alignment: .top) {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Lifetime Pro")
                                .font(.system(size: 24, weight: .black, design: .rounded))
                            Text("One-time purchase")
                                .font(.system(size: 15, weight: .bold, design: .rounded))
                                .foregroundStyle(accent)
                        }

                        Spacer()

                        lifetimePricePanel(product)
                            .frame(width: 164)
                    }

                    VStack(alignment: .leading, spacing: 12) {
                        benefitLine(icon: "infinity", text: "Unlimited toxicity scans")
                        benefitLine(icon: "bolt.fill", text: "Instant AI recognition")
                        benefitLine(icon: "wifi.slash", text: "Offline database access")
                        benefitLine(icon: "checkmark.seal.fill", text: "Expert-verified reports")
                        benefitLine(icon: "arrow.triangle.2.circlepath", text: "Continuous optimization")
                    }

                    VStack(spacing: 10) {
                        RoundedRectangle(cornerRadius: 999, style: .continuous)
                            .fill(Color(red: 0.14, green: 0.58, blue: 0.18))
                            .frame(height: 10)

                        HStack {
                            Text("COST PER SCAN: $0.00")
                            Spacer()
                            Text("INFINITE VALUE")
                                .foregroundStyle(accent)
                        }
                        .font(.system(size: 12, weight: .bold, design: .rounded))
                        .foregroundStyle(tertiaryText)
                    }
                }
                .foregroundStyle(primaryText)
                .padding(18)
                .background(
                    cardBackground,
                    in: .rect(cornerRadius: 18)
                )
                .overlay {
                    RoundedRectangle(cornerRadius: 18, style: .continuous)
                        .stroke(
                            isSelected(product)
                                ? Color(red: 0.14, green: 0.58, blue: 0.18)
                                : lifetimeCardBorder,
                            lineWidth: isSelected(product) ? 2.4 : 1.6
                        )
                }
                .shadow(
                    color: .black.opacity(isDark ? 0.26 : 0.05),
                    radius: 6,
                    x: 0,
                    y: 3
                )
                .overlay(alignment: .topTrailing) {
                    Text("LIMITED OFFER")
                        .font(.system(size: 12, weight: .black, design: .rounded))
                        .foregroundStyle(.white)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 6)
                        .background(
                            Color(red: 0.16, green: 0.54, blue: 0.20),
                            in: Capsule()
                        )
                        .offset(x: -10, y: -12)
                }
            }
            .buttonStyle(.plain)
            .disabled(isUnavailable(product))
            .opacity(isUnavailable(product) ? 0.65 : 1)
            .accessibilityAddTraits(isSelected(product) ? .isSelected : [])
        }
    }

    private func lifetimePricePanel(_ product: PaywallProduct) -> some View {
        VStack(alignment: .trailing, spacing: 4) {
            Text(referencePrice(for: product))
                .font(.system(size: 13, weight: .bold, design: .rounded))
                .foregroundStyle(.white.opacity(0.74))
                .strikethrough()
                .lineLimit(1)
                .minimumScaleFactor(0.58)
                .allowsTightening(true)

            Text(price(for: product, fallback: "$27.99"))
                .font(.system(size: 28, weight: .black, design: .rounded))
                .monospacedDigit()
                .foregroundStyle(.white)
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .allowsTightening(true)
                .layoutPriority(1)
        }
        .frame(maxWidth: .infinity, alignment: .trailing)
        .padding(.horizontal, 14)
        .padding(.vertical, 14)
        .background(
            LinearGradient(
                colors: [
                    Color(red: 0.16, green: 0.54, blue: 0.20),
                    Color(red: 0.10, green: 0.40, blue: 0.16)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            ),
            in: .rect(cornerRadius: 18)
        )
    }

    private func benefitLine(icon: String, text: String) -> some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(
                        isDark
                            ? Color(red: 0.21, green: 0.29, blue: 0.22)
                            : Color(red: 0.86, green: 0.92, blue: 0.86)
                    )
                Image(systemName: icon)
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(accent)
            }
            .frame(width: 34, height: 34)

            Text(text)
                .font(.system(size: 15, weight: .bold, design: .rounded))
                .foregroundStyle(primaryText)
        }
    }

    private var creditSection: some View {
        VStack(spacing: 12) {
            Text("PAY ONLY FOR WHAT YOU USE")
                .font(.system(size: 13, weight: .black, design: .rounded))
                .tracking(3)
                .foregroundStyle(secondaryText)
                .frame(maxWidth: .infinity)
                .padding(.top, 4)

            LazyVGrid(
                columns: [
                    GridItem(.flexible(), spacing: 12),
                    GridItem(.flexible(), spacing: 12)
                ],
                spacing: 12
            ) {
                ForEach(creditProducts) { product in
                    Button {
                        store.select(product.id)
                    } label: {
                        creditPackCard(product)
                    }
                    .buttonStyle(.plain)
                    .disabled(isUnavailable(product))
                    .opacity(isUnavailable(product) ? 0.55 : 1)
                    .accessibilityAddTraits(isSelected(product) ? .isSelected : [])
                }
            }
        }
    }

    private func creditPackCard(_ product: PaywallProduct) -> some View {
        let count = creditCount(for: product)
        let isPopular = count == 20

        return VStack(spacing: 10) {
            if isPopular {
                Text("SAVE 25%")
                    .font(.system(size: 10, weight: .black, design: .rounded))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(Color(red: 0.08, green: 0.14, blue: 0.26), in: Capsule())
                    .offset(y: -6)
                    .padding(.bottom, -8)
            }

            Text(isPopular ? "POPULAR" : "STARTER")
                .font(.system(size: 15, weight: .black, design: .rounded))
                .foregroundStyle(secondaryText)
            Text("\(count) Scans")
                .font(.system(size: 20, weight: .black, design: .rounded))
                .foregroundStyle(primaryText)
            Text(price(for: product, fallback: isPopular ? "$2.99" : "$0.99"))
                .font(.system(size: 24, weight: .black, design: .rounded))
                .foregroundStyle(accent)
            Text(unitPrice(for: product, count: count))
                .font(.system(size: 12, weight: .semibold, design: .rounded))
                .foregroundStyle(secondaryText)
        }
        .frame(maxWidth: .infinity, minHeight: 176)
        .padding(.vertical, 8)
        .background(
            cardBackground,
            in: .rect(cornerRadius: 16)
        )
        .overlay {
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(
                    isSelected(product)
                        ? Color(red: 0.14, green: 0.58, blue: 0.18)
                        : cardBorder,
                    lineWidth: isSelected(product) ? 2.2 : 1.2
                )
        }
        .shadow(
            color: .black.opacity(isDark ? 0.20 : 0.04),
            radius: 4,
            x: 0,
            y: 2
        )
    }

    private var bottomCTA: some View {
        Button {
            Task {
                await store.purchaseSelectedProduct()
            }
        } label: {
            HStack(spacing: 10) {
                if store.isBusy {
                    ProgressView()
                        .tint(.white)
                }

                Text(bottomCTATitle)
                    .font(.system(size: 18, weight: .black, design: .rounded))

                if !isLifetimeMember {
                    Image(systemName: "arrow.right")
                        .font(.system(size: 18, weight: .bold))
                }
            }
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 18)
            .background(
                isLifetimeMember
                    ? Color(red: 0.25, green: 0.35, blue: 0.28)
                    : Color(red: 0.17, green: 0.54, blue: 0.20),
                in: .rect(cornerRadius: 16)
            )
            .overlay {
                PaywallShimmerOverlay(
                    active: !isLifetimeMember && !store.isBusy
                )
            }
            .shadow(
                color: Color(red: 0.10, green: 0.32, blue: 0.14).opacity(0.35),
                radius: 10,
                x: 0,
                y: 6
            )
        }
        .buttonStyle(.plain)
        .disabled(isPrimaryButtonDisabled)
        .opacity(isPrimaryButtonDisabled ? 0.85 : 1)
    }

    private var bottomCTATitle: String {
        if isLifetimeMember {
            return "Lifetime Activated"
        }
        guard let product = store.selectedProduct else {
            return "Pay once, enjoy forever"
        }
        switch product.kind {
        case .lifetime:
            return "Pay once, enjoy forever"
        case .credits(let count):
            return "Buy \(count) Scans for \(price(for: product, fallback: count == 20 ? "$2.99" : "$0.99"))"
        }
    }

    private var isPrimaryButtonDisabled: Bool {
        if isLifetimeMember || store.isBusy {
            return true
        }
        guard let selectedID = store.selectedProductID else {
            return true
        }
        return store.productDetails(for: selectedID) == nil
    }

    private var lifetimeProduct: PaywallProduct? {
        configuration.catalog.products.first {
            if case .lifetime = $0.kind {
                return true
            }
            return false
        }
    }

    private var creditProducts: [PaywallProduct] {
        configuration.catalog.products
            .filter {
                if case .credits = $0.kind {
                    return true
                }
                return false
            }
            .sorted { creditCount(for: $0) < creditCount(for: $1) }
    }

    private var isLifetimeMember: Bool {
        store.entitlement == .lifetime
    }

    private func isSelected(_ product: PaywallProduct) -> Bool {
        store.selectedProductID == product.id
    }

    private func isUnavailable(_ product: PaywallProduct) -> Bool {
        isLifetimeMember || store.isBusy || store.productDetails(for: product.id) == nil
    }

    private func creditCount(for product: PaywallProduct) -> Int {
        guard case .credits(let count) = product.kind else {
            return 0
        }
        return count
    }

    private func price(for product: PaywallProduct, fallback: String) -> String {
        store.productDetails(for: product.id)?.localizedPrice ?? fallback
    }

    private func referencePrice(for product: PaywallProduct) -> String {
        if let price = store.productDetails(for: product.id)?.price {
            let doubledPrice = price * 2
            let formatter = NumberFormatter()
            formatter.numberStyle = .currency
            formatter.currencyCode = store.productDetails(for: product.id)?.currencyCode
            if let formatted = formatter.string(from: NSDecimalNumber(decimal: doubledPrice)) {
                return formatted
            }
        }
        return product.originalPriceText ?? "$55.98"
    }

    private func unitPrice(for product: PaywallProduct, count: Int) -> String {
        if let localized = store.productDetails(for: product.id)?
            .localizedUnitPrice(dividingBy: count) {
            return "\(localized) per scan"
        }
        return count == 20 ? "$0.15 per scan" : "$0.20 per scan"
    }

    private var isDark: Bool {
        colorScheme == .dark
    }

    private var accent: Color {
        Color(red: 0.12, green: 0.52, blue: 0.18)
    }

    private var pageBackground: Color {
        isDark
            ? Color(red: 0.08, green: 0.10, blue: 0.12)
            : Color(red: 0.95, green: 0.96, blue: 0.95)
    }

    private var primaryText: Color {
        isDark
            ? Color(red: 0.93, green: 0.95, blue: 0.98)
            : Color(red: 0.08, green: 0.12, blue: 0.20)
    }

    private var secondaryText: Color {
        isDark
            ? Color(red: 0.62, green: 0.68, blue: 0.76)
            : Color(red: 0.58, green: 0.64, blue: 0.72)
    }

    private var tertiaryText: Color {
        Color(red: 0.52, green: 0.58, blue: 0.66)
    }

    private var cardBackground: Color {
        isDark
            ? Color(red: 0.13, green: 0.15, blue: 0.18)
            : .white
    }

    private var cardBorder: Color {
        isDark
            ? .white.opacity(0.14)
            : Color(red: 0.84, green: 0.88, blue: 0.91)
    }

    private var lifetimeCardBorder: Color {
        isDark
            ? Color(red: 0.31, green: 0.48, blue: 0.33)
            : Color(red: 0.67, green: 0.82, blue: 0.67)
    }
}

private struct LayoutMetrics {
    let width: CGFloat

    var isWideScreen: Bool {
        width >= 900
    }

    var contentMaxWidth: CGFloat {
        isWideScreen ? .infinity : 760
    }

    var horizontalPadding: CGFloat {
        isWideScreen ? 24 : 16
    }

    let sectionSpacing: CGFloat = 20
}

private struct PaywallShimmerOverlay: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var phase: CGFloat = -1.2

    let active: Bool

    var body: some View {
        GeometryReader { proxy in
            LinearGradient(
                colors: [
                    .clear,
                    .white.opacity(0.28),
                    .clear
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .frame(width: max(proxy.size.width * 0.28, 48))
            .rotationEffect(.degrees(18))
            .offset(x: phase * proxy.size.width)
        }
        .clipShape(.rect(cornerRadius: 16))
        .allowsHitTesting(false)
        .opacity(active && !reduceMotion ? 1 : 0)
        .onAppear {
            startAnimationIfNeeded()
        }
        .onChange(of: active) { _ in
            startAnimationIfNeeded()
        }
    }

    private func startAnimationIfNeeded() {
        guard active, !reduceMotion else {
            phase = -1.2
            return
        }
        phase = -1.2
        withAnimation(.linear(duration: 1.35).repeatForever(autoreverses: false)) {
            phase = 1.4
        }
    }
}
