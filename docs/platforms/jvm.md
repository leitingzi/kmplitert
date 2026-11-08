# JVM

`jvmMain` 使用 JNA 调用 LiteRT C API。JVM 与 Kotlin/Native 桌面目标是不同的构建目标，依赖加载方式也不同。

## 模型文件

将模型放到本地可读文件，并将绝对路径传给[推理示例](../getting-started.md)。若模型打包在应用 classpath 或 JAR 中，先将资源复制到文件；不要把 classpath 资源名当作系统文件路径。

## 原生库

当前仓库的 JVM 资源目录包含 Windows x86-64、Linux x86-64 / ARM64 和 macOS ARM64 的原生库。加载器按当前平台提取并加载对应库。具体发布制品是否包含目标二进制，需检查所使用版本。

也可以通过 JVM 系统属性指定原生主库文件或包含主库的目录：

```text
-Dkmplitert.native.path=/absolute/path/to/litert-libraries
```

所指定的主库、可选加速插件及其依赖需要匹配操作系统、架构和 LiteRT ABI。主库加载失败会报错；加速插件不可用时会记录警告，GPU 请求可能回退 CPU。该选项应在首次加载运行时前设置。

## 生命周期与测试

JVM 实现序列化编译器操作，并在关闭时释放其跟踪的缓冲区；不要将 JVM 的行为推广到其他平台。仓库提供 JVM 温度模型和 ABI 测试，但现有 Core CI 未列出 JVM 测试运行步骤。

常见加载问题见[故障排查](../troubleshooting.md)。
