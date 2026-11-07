# KMPLiteRT

**KMPLiteRT** 是一款面向 Kotlin Multiplatform 的 LiteRT（原 TensorFlow Lite）推理库。  
该库提供跨平台统一的 Kotlin API，支持 Android、iOS、JVM、Windows、Linux、macOS、JS 以及 WasmJS平台。  
可供应用加载 LiteRT 模型、准备张量数据并执行本地推理计算。

## 能做什么

- 在共享 Kotlin 代码中定义推理流程，由各平台适配层对接底层 LiteRT 运行时。
- **Core 模块**：通过 `LiteRTCompiler` 执行模型推理，借助 `TFBuffer` 完成张量读写。
- 可按需选用 CPU、GPU、NPU 加速器；硬件加速能力受目标平台与模型本身限制。
- **Tool 模块（可选）**：封装数据预处理与后处理逻辑，例如图像转模型输入张量、模型输出结果解析转换。

## 文档导航

- [安装与快速开始](getting-started.md)：添加依赖并运行一次基础推理。
- [平台与运行环境](platforms.md)：了解目标平台和平台相关配置。
- [Core API](api.md)：`LiteRTCompiler`、`TFBuffer` 与 `LiteRTHandler` 的用法。
- [Native TFBuffer](native-buffer.md)：Native 实现的缓冲区锁定、读写和资源生命周期。
- [支持与贡献](support.md)：报告问题或参与项目。

## 项目信息

| 项目 | 信息 |
| --- | --- |
| Maven group | `io.github.leitingzi` |
| 当前版本 | `0.3.0` |
| 仓库 | [github.com/leitingzi/kmplitert](https://github.com/leitingzi/kmplitert) |
| 许可证 | [Apache License 2.0](https://github.com/leitingzi/kmplitert/blob/master/LICENSE) |

> 文档与实现可能随版本演进。请以你所使用版本的 API 和平台配置为准。
