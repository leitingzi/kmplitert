# Platforms and Capabilities

Core exposes a shared interface and connects it to each platform's LiteRT runtime. A common interface does not mean model paths, hardware capabilities, and resource management are identical.

## Implementations and targets

| Platform guide | Source sets | Configured targets | Runtime |
|---|---|---|---|
| [Android](platforms/android.md) | `androidMain` | Android, minimum API 24 | Google LiteRT Android |
| [JVM](platforms/jvm.md) | `jvmMain` | JVM | JNA + LiteRT C API |
| [iOS / macOS](platforms/apple.md) | `nativeMain` / `nativeInterop` | `iosArm64`, `iosSimulatorArm64`, `macosArm64` | LiteRT C API |
| [Linux / Windows Native](platforms/native-desktop.md) | `nativeMain` / `nativeInterop` | `linuxX64`, `linuxArm64`, `mingwX64` | LiteRT C API |
| [JavaScript](platforms/javascript.md) | `webMain` / `jsMain` | Browser JS | `@litertjs/core` |
| [WasmJS](platforms/wasmjs.md) | `webMain` / `wasmJsMain` | Browser WasmJS | `@litertjs/core` |

Native targets are configured according to the host or publication task. A configured target does not mean every device, accelerator, or published artifact has been verified. See the [Core build configuration](https://github.com/leitingzi/kmplitert/blob/master/core/build.gradle.kts).

## Verification scope

The repository contains CPU temperature-conversion tests for Android devices, JVM, Native, JS, and WasmJS. Core CI configures Linux, macOS, iOS simulator, Windows Native, and browser tests and builds Android; the Android device and JVM tests are **not listed as execution steps in that workflow**. The existence of tests or CI configuration does not prove that the current version passed in every environment. Check recent runs and test your target devices.

## Differences to note {#differences}

- **Signatures:** Android, JVM, and Native accept a signature index for buffer creation and execution. Native name-based metadata queries use signature 0; JS/WasmJS currently ignore the buffer and run methods' signature index.
- **Acceleration:** CPU/GPU/NPU are requests, not hardware guarantees. See [hardware acceleration](guide/acceleration.md).
- **Paths:** Local platforms use file paths; the web uses a browser-accessible model URL and runtime assets.
- **Resources:** Native tests explicitly close buffers; do not assume ownership or repeated-close behavior is identical across platforms.
