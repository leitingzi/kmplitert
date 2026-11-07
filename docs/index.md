# KMPLiteRT

**KMPLiteRT** 是一个面向 Kotlin Multiplatform 的 LiteRT（原 TensorFlow Lite）推理库。它为不同平台提供统一的 Kotlin API，让应用可以加载 LiteRT 模型、准备张量数据并执行本地推理。

## 能做什么

- 在共享 Kotlin 代码中编写推理流程，并由平台实现负责调用对应运行时。
- 通过 `LiteRTCompiler` 执行模型推理，通过 `TFBuffer` 读写张量数据。
- 按需选择 `CPU`、`GPU` 或 `NPU` 加速器（具体可用能力取决于目标平台和模型）。
- 使用可选的 `kmplitert-tool` 模块处理更高层的任务流程和输入预处理。

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
