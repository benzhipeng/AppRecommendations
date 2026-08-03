# BioScanKit

BioScanKit provides the shared feature boundaries used by the BioScan apps, with native implementations for each platform.

## Platform layout

- **iOS:** `Package.swift`, `Sources/`, and `Tests/` at this directory root. Keeping the Swift Package at the existing path preserves current Xcode integrations.
- **Android:** `Android/`, an Android Library module implemented with Jetpack Compose.

Both implementations use iNature as the canonical default behavior and visual theme. App-specific recognition engines, navigation, analytics, product identifiers, credentials, and branded assets stay in each host app and are injected through configuration, interfaces, callbacks, and content slots.
