// swift-tools-version:5.1

import PackageDescription

let package = Package(
    name: "NimbusPluginBarcodeScanner",
    platforms: [.iOS(.v11)],
    products: [
        .library(
            name: "NimbusPluginBarcodeScanner",
            targets: ["NimbusPluginBarcodeScanner"]),
    ],
    dependencies: [
        .package(url: "https://github.com/salesforce/nimbus", .branch("main"))
    ],
    targets: [
        .target(
            name: "NimbusPluginBarcodeScanner",
            dependencies: ["Nimbus"],
            path: "platforms/ios/Sources/NimbusPluginBarcodeScanner"),
    ]
)
