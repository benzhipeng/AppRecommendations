import SwiftUI
import UIKit

public extension View {
    /// Applies the shared navigation style for views pushed onto a navigation stack.
    ///
    /// The style keeps the native back action and interactive pop gesture, displays
    /// only the back chevron, centers the title, and removes the toolbar separator.
    func bioScanPushNavigation(title: String) -> some View {
        modifier(BioScanPushNavigationModifier(title: title))
    }
}

private struct BioScanPushNavigationModifier: ViewModifier {
    let title: String

    func body(content: Content) -> some View {
        content
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar(.visible, for: .navigationBar)
            .toolbarBackground(.hidden, for: .navigationBar)
            .background {
                MinimalBackButtonConfigurator()
                    .frame(width: 0, height: 0)
            }
    }
}

private struct MinimalBackButtonConfigurator: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> Controller {
        Controller()
    }

    func updateUIViewController(_ uiViewController: Controller, context: Context) {
        uiViewController.applyBackButtonStyle()
    }

    final class Controller: UIViewController {
        private weak var configuredNavigationBar: UINavigationBar?
        private var tintObservation: NSKeyValueObservation?

        override func didMove(toParent parent: UIViewController?) {
            super.didMove(toParent: parent)
            applyBackButtonStyle()
        }

        override func viewWillAppear(_ animated: Bool) {
            super.viewWillAppear(animated)
            applyBackButtonStyle()
        }

        func applyBackButtonStyle() {
            configureNavigationItems()
            DispatchQueue.main.async { [weak self] in
                self?.configureNavigationItems()
            }
        }

        private func configureNavigationItems() {
            guard let navigationBar = navigationController?.navigationBar else { return }
            observeTintChanges(on: navigationBar)
            applyNavigationTint(to: navigationBar)
            navigationBar.topItem?.backButtonDisplayMode = .minimal
            navigationBar.backItem?.backButtonDisplayMode = .minimal
        }

        private func observeTintChanges(on navigationBar: UINavigationBar) {
            guard configuredNavigationBar !== navigationBar else { return }

            tintObservation = nil
            configuredNavigationBar = navigationBar
            tintObservation = navigationBar.observe(\.tintColor, options: [.new]) { [weak self] navigationBar, _ in
                self?.applyNavigationTint(to: navigationBar)
            }
        }

        private func applyNavigationTint(to navigationBar: UINavigationBar) {
            let expectedColor = UIColor.label.resolvedColor(with: navigationBar.traitCollection)
            let currentColor = navigationBar.tintColor.resolvedColor(with: navigationBar.traitCollection)
            guard !currentColor.isEqual(expectedColor) else { return }
            navigationBar.tintColor = .label
        }
    }
}
