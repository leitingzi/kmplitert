# WasmJS

`wasmJsMain` and `webMain` call `@litertjs/core` through Kotlin/Wasm JS interop in a browser. The current npm dependency is version `2.6.0`.

## Model and runtime

As with [JavaScript](javascript.md), serve the model as a static web resource and provide a browser-accessible URL such as `/models/CelsiusToFahrenheit.tflite`. Adjust the URL for sites deployed under a subpath.

The first model initialization loads the web runtime and WASM assets. Configure self-hosted assets using [`LiteRtInit`](../api/web.md) before initialization starts; the assets must match the npm package version.

## Acceleration and limitations

CPU uses WASM; GPU requests select WASM when WebGPU is unavailable; NPU requests use WebNN options. Verify actual browser capability. The current implementation does not use `signatureIndex` to select a signature.

Write inputs before running and read outputs after it completes. The repository includes browser CPU inference tests; their `/base/...` paths are test-server paths and should not be copied into production.
