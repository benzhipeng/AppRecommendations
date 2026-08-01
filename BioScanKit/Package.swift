// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "BioScanKit",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(name: "BioScanDesign", targets: ["BioScanDesign"]),
        .library(name: "BioScanSettings", targets: ["BioScanSettings"]),
        .library(name: "BioScanCapture", targets: ["BioScanCapture"]),
        .library(name: "BioScanPaywall", targets: ["BioScanPaywall"]),
        .library(
            name: "BioScanKit",
            targets: [
                "BioScanDesign",
                "BioScanSettings",
                "BioScanCapture",
                "BioScanPaywall"
            ]
        )
    ],
    targets: [
        .target(name: "BioScanDesign"),
        .target(
            name: "BioScanSettings",
            dependencies: ["BioScanDesign"]
        ),
        .target(
            name: "BioScanCapture",
            dependencies: ["BioScanDesign"]
        ),
        .target(
            name: "BioScanPaywall",
            dependencies: ["BioScanDesign"]
        ),
        .testTarget(
            name: "BioScanCaptureTests",
            dependencies: ["BioScanCapture"]
        ),
        .testTarget(
            name: "BioScanPaywallTests",
            dependencies: ["BioScanPaywall"]
        ),
        .testTarget(
            name: "BioScanSettingsTests",
            dependencies: ["BioScanSettings"]
        )
    ]
)
