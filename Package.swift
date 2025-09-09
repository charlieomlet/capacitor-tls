// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorTls",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CapacitorTls",
            targets: ["CapacitorTlsPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "CapacitorTlsPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/CapacitorTlsPlugin"),
        .testTarget(
            name: "CapacitorTlsPluginTests",
            dependencies: ["CapacitorTlsPlugin"],
            path: "ios/Tests/CapacitorTlsPluginTests")
    ]
)