# 硬件加速

创建编译器时可请求 `LiteRTAccelerator.CPU`、`GPU` 或 `NPU`，默认是 CPU。

```kotlin
val compiler = LiteRTCompiler(modelPath, LiteRTAccelerator.CPU)
```

这些值表达请求，不保证设备、模型算子、运行时、插件或浏览器都支持对应硬件。请先使用 CPU 建立正确性基线，再在目标设备上测试加速效果和回退行为。

| 实现 | 当前行为与注意事项 |
|---|---|
| Android | 将选择传递给 Android LiteRT；具体支持取决于运行时和设备。 |
| JVM | 映射到 LiteRT 硬件选项；原生加速插件不可用时加载器会记录 GPU 回退到 CPU 的警告。 |
| Native | 映射到 LiteRT C API 硬件选项；插件、动态库和设备能力需匹配。 |
| JS / WasmJS | CPU 使用 WASM；GPU 请求在 WebGPU 不可用时选择 WASM；NPU 请求使用 WebNN 选项。WebNN 可用性仍需在目标浏览器验证。 |

初始化成功并不必然证明模型全部运行在指定硬件上。对于性能敏感的场景，分别测量初始化、数据传输和多次推理，并检查运行时日志。
