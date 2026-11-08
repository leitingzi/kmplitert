# iOS and macOS Native

Apple targets call the LiteRT C API through Kotlin/Native C interop. The current Core build configures `iosArm64`, `iosSimulatorArm64`, and `macosArm64` on macOS hosts or under publication conditions.

## Models and binaries

Place the model in application resources or another accessible location, and pass an actual filesystem path to `LiteRTCompiler`. How you obtain a resource path depends on the iOS/macOS project.

LiteRT libraries must match the device or simulator architecture, operating system, and C ABI. Repository build conventions configure linking paths and dynamic-library bundling for these targets; this is internal build logic and should not be assumed available automatically to external consumers.

For the final application, check framework linkage, library embedding, signing, and runtime search paths. The repository's [iOS / Native setup example](https://github.com/leitingzi/kmplitert#-ios--native-setup-critical) is a starting point, not a universal configuration: validate it against your Xcode project, LiteRT binaries, and release version.

## Verification

Core CI configures macOS ARM64 and iOS ARM64 simulator tests. Verify physical devices, other models, and GPU/NPU capabilities separately on actual targets.

Manage Native buffers as described in the [lifecycle guide](../guide/lifecycle.md); name-based metadata queries currently use signature 0.
