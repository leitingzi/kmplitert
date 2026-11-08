# 平台与能力

Core 在公共源集提供统一接口，由平台源集对接各自的 LiteRT 运行时。相同接口不意味着模型路径、硬件能力和资源管理完全相同。

## 实现与目标

| 平台指南 | 实现源集 | 构建配置中的目标 | 运行时 |
|---|---|---|---|
| [Android](platforms/android.md) | `androidMain` | Android，最低 API 24 | Google LiteRT Android |
| [JVM](platforms/jvm.md) | `jvmMain` | JVM | JNA + LiteRT C API |
| [iOS / macOS](platforms/apple.md) | `nativeMain` / `nativeInterop` | `iosArm64`、`iosSimulatorArm64`、`macosArm64` | LiteRT C API |
| [Linux / Windows Native](platforms/native-desktop.md) | `nativeMain` / `nativeInterop` | `linuxX64`、`linuxArm64`、`mingwX64` | LiteRT C API |
| [JavaScript](platforms/javascript.md) | `webMain` / `jsMain` | 浏览器 JS | `@litertjs/core` |
| [WasmJS](platforms/wasmjs.md) | `webMain` / `wasmJsMain` | 浏览器 WasmJS | `@litertjs/core` |

Native 目标按构建宿主系统或发布任务条件配置；目标的存在不代表所有设备、加速器或已发布制品都经过验证。具体目标以 [Core 构建配置](https://github.com/leitingzi/kmplitert/blob/master/core/build.gradle.kts)为准。

## 验证范围

仓库有 Android 设备、JVM、Native、JS 和 WasmJS 的 CPU 温度转换测试。现有 Core CI 配置执行 Linux、macOS、iOS 模拟器、Windows Native 和浏览器测试，并构建 Android；Android 设备测试和 JVM 测试**未在该工作流中列为运行步骤**。测试代码存在或 CI 配置存在，均不代表当前版本在所有环境中已经通过。查看最新运行结果并在目标设备上验证。

## 需要留意的差异 {#differences}

- **签名：**Android、JVM 和 Native 的缓冲区创建与运行接收签名索引；Native 按名称查询元数据固定使用签名 0；JS/WasmJS 当前忽略缓冲区和运行方法的签名索引。
- **加速：**CPU/GPU/NPU 是请求，不保证硬件可用；参见[硬件加速](guide/acceleration.md)。
- **路径：**本地平台使用文件路径；Web 使用浏览器可访问的模型 URL 和运行时资源。
- **资源：**Native 测试显式关闭缓冲区；不要假设所有平台的所有权和重复关闭行为相同。
