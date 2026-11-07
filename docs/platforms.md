# 平台与运行环境

KMPLiteRT 在不同 Kotlin Multiplatform source set 中提供平台实现。各平台的模型文件访问方式、原生运行时和硬件委托并不完全相同。

| 平台类别 | 项目中的实现 source set | 使用说明 |
| --- | --- | --- |
| Android | `androidMain` | 使用 Android LiteRT 运行时；使用 Android assets 时需初始化 `LiteRTFileUtils`。 |
| JVM | `jvmMain` | 使用 JNA 与随库提供的 LiteRT 原生库。 |
| Apple | `nativeMain` / Apple Native targets | 通过 LiteRT Native C API；Apple 构建需要配置匹配架构的原生库。 |
| Linux / Windows | `nativeMain` / 对应 Native targets | 使用 LiteRT Native C API；原生库必须匹配操作系统和架构。 |
| JavaScript | `jsMain` | 使用 `@litertjs/core`。 |
| WebAssembly | `wasmJsMain` | 使用 WebAssembly JS 目标及对应 LiteRT Web 运行时。 |

## Apple Native 配置

Apple Native 构建需为目标架构准备匹配的 LiteRT 原生库，并让最终应用能够找到这些动态库。iOS 应用还需检查 framework 的链接方式和运行时搜索路径。项目 README 中提供了完整的 Gradle 配置示例及库文件目录约定：

[查看 README 中的 iOS & Native 配置](https://github.com/leitingzi/kmplitert#-ios--native-setup-critical)

不要把不同平台或架构的二进制库混用。构建与分发前，请确认 LiteRT 二进制版本、C ABI 和目标架构一致。

## 硬件加速

API 提供 `CPU`、`GPU`、`NPU` 三种加速器选择。具体后端能力取决于目标平台和设备；例如平台上的 GPU/NPU delegate 并非自动可用。不要将某一设备上的加速结果视为其他设备的保证。
