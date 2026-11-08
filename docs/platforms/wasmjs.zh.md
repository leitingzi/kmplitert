# WasmJS

`wasmJsMain` 与 `webMain` 使用 Kotlin/Wasm JS interop 调用 `@litertjs/core`，目标是浏览器 WasmJS。当前 npm 依赖版本为 `2.6.0`。

## 模型与运行时

像 [JavaScript](javascript.md) 一样，将模型作为 Web 静态资源发布，并传入浏览器可访问的 URL，例如 `/models/CelsiusToFahrenheit.tflite`。站点部署在子路径时，请调整 URL。

首次模型初始化会加载 Web 运行时和 WASM 资源。可在首次初始化之前通过 [`LiteRtInit`](../api/web.md) 配置自托管资源；所部署资源应与 npm 包版本匹配。

## 加速与限制

CPU 使用 WASM；GPU 请求在 WebGPU 不可用时选择 WASM；NPU 请求使用 WebNN 选项。实际浏览器能力必须自行验证。当前实现没有使用 `signatureIndex` 参数选择签名。

输入必须在运行前写入，输出应在运行后读取。仓库有浏览器 CPU 推理测试；测试使用的 `/base/...` 是测试服务器路径，不应照搬到生产应用。
