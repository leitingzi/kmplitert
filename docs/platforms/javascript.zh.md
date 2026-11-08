# JavaScript

`jsMain` 与 `webMain` 使用 `@litertjs/core` 在浏览器中加载并运行模型。当前构建配置使用该 npm 包的 `2.6.0` 版本。

## 模型与运行时

将模型放入应用的 Web 静态资源目录，通过 Web 服务器提供，并将可访问的 URL 传给[推理示例](../getting-started.md)，例如 `/models/CelsiusToFahrenheit.tflite`。实际 URL 必须与部署的站点根路径一致；跨域加载还需要服务器允许。

`LiteRTCompiler.init()` 会等待共享 Web 运行时初始化。默认 WASM 资源从 CDN 加载；离线或受限网络环境可在第一次初始化之前配置[自托管路径](../api/web.md)。

## 加速与限制

CPU 使用 WASM；请求 GPU 时如果 WebGPU 不可用，会选择 WASM；NPU 请求使用 WebNN 选项。浏览器、设备、模型和服务器环境都会影响结果。当前实现没有使用 `signatureIndex` 参数选择签名。

输入写入会创建 JS 张量，输出在 `run()` 后填充。仓库有浏览器 CPU 推理测试；测试框架中的 `/base/...` 路径是测试服务器专用路径，不是应用部署路径。
