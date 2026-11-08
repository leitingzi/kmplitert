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

## 维护文档

安装 [requirements-docs.txt](https://github.com/leitingzi/kmplitert/blob/master/requirements-docs.txt) 中的文档依赖，然后在仓库根目录执行：

```shell
python -m mkdocs serve
python -m mkdocs build --strict
python .github/scripts/check_docs_i18n.py
```

英文页面使用 `*.md`，对应的中文页面使用 `*.zh.md`。请保持两种语言的内容、链接及共享锚点同步。文档部署工作流在 `master` 上的文档相关变更后运行严格构建并发布到 GitHub Pages。修改 Core 版本、目标、运行时、API 或测试后，请同步检查安装示例、平台能力矩阵和限制说明；修改快速开始中的推理代码时，也要同步更新 JVM 文档示例测试。未验证的设备与加速器不要标记为已通过。

## 许可证

KMPLiteRT 使用 [Apache License 2.0](https://github.com/leitingzi/kmplitert/blob/master/LICENSE)。
