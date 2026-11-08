# JavaScript

`jsMain` and `webMain` use `@litertjs/core` to load and run models in the browser. The current build uses npm package version `2.6.0`.

## Model and runtime

Put the model in your application's web static resources, serve it via a web server, and pass an accessible URL such as `/models/CelsiusToFahrenheit.tflite` to the [inference example](../getting-started.md). The URL must match your deployment base path; cross-origin loading also requires server permission.

`LiteRTCompiler.init()` waits for shared web runtime initialization. WASM assets load from a CDN by default; offline or restricted environments can configure a [self-hosted path](../api/web.md) before initialization starts.

## Acceleration and limitations

CPU uses WASM; GPU requests select WASM when WebGPU is unavailable; NPU requests use WebNN options. Browser, device, model, and server environment affect results. The current implementation does not use `signatureIndex` to select a signature.

Writing inputs creates JS tensors; outputs are populated after `run()`. The repository contains browser CPU tests. Their `/base/...` paths belong to the test server, not application deployments.
