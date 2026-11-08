# iOS 与 macOS Native

Apple 目标通过 Kotlin/Native C interop 调用 LiteRT C API。当前 Core 构建配置包含 `iosArm64`、`iosSimulatorArm64` 和 `macosArm64`，并在 macOS 宿主或发布任务条件下配置。

## 模型与二进制

将模型放入应用资源或其他可访问位置，最终向 `LiteRTCompiler` 传入实际文件系统路径。应用资源路径的获取方式取决于具体 iOS/macOS 工程。

LiteRT 库必须与设备或模拟器目标架构、操作系统及 C ABI 匹配。仓库构建约定为这些目标配置了链接路径和动态库打包逻辑；它是仓库内部 build-logic，不应假设在外部消费项目中自动可用。

对最终应用，检查 framework 链接方式、动态库嵌入、签名和运行时搜索路径。仓库 [iOS / Native 配置示例](https://github.com/leitingzi/kmplitert#-ios--native-setup-critical)可作为起点，但需要结合使用的 Xcode 工程、LiteRT 二进制和发布版本验证，不能直接视为所有应用的通用配置。

## 验证

仓库 Core CI 配置了 macOS ARM64 和 iOS ARM64 模拟器测试。真机、不同模型以及 GPU/NPU 能力需要在实际目标上单独验证。

Native 缓冲区应按[生命周期指南](../guide/lifecycle.md)管理；按名称查询元数据当前使用签名 0。
