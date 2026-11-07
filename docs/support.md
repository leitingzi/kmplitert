# 支持与贡献

## 报告问题

请在 [GitHub Issues](https://github.com/leitingzi/kmplitert/issues) 提交问题，并尽可能提供：

- KMPLiteRT 版本、目标平台和设备架构。
- Kotlin、Gradle 与 LiteRT 运行时版本。
- 最小复现步骤、模型输入输出信息和完整错误堆栈。
- 是否只在特定 delegate（CPU/GPU/NPU）下发生。

提交模型或设备信息前，请确认其中不含隐私或敏感数据。

## 贡献代码

1. Fork 仓库并创建功能分支。
2. 修改代码或文档，并运行相关测试。
3. 提交清晰的变更说明并发起 Pull Request。

在更改平台实现时，请尽量让共享 API 行为保持一致，并在可用的目标平台上验证。

## 许可证

KMPLiteRT 使用 [Apache License 2.0](https://github.com/leitingzi/kmplitert/blob/master/LICENSE)。
