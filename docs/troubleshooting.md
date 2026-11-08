# Troubleshooting

Check the Core version, target platform, model, and inputs, then reproduce with CPU. Error logs and the specific platform are usually more useful than a generic error message.

| Symptom | Possible cause | What to check |
|---|---|---|
| Model fails to load | Incorrect path, missing file, or corrupt model | Check local file permissions and size; on the web inspect the model URL's request and cross-origin response. |
| JVM cannot load the LiteRT library | Missing library, incompatible architecture or ABI | Check the package and process architecture; set `kmplitert.native.path` if needed. See the [JVM guide](platforms/jvm.md). |
| Native linking or startup fails | Link path, deployed libraries, or dependencies do not match | Check architecture, libraries and search paths, and Apple embedding and signing. |
| Web initialization fails | CDN unavailable, incorrect WASM path, or version mismatch | Inspect browser network requests; configure a [self-hosted path](api/web.md) before initialization if needed. |
| Tensor not found | Input/output name or signature mismatch | Use the model's names and note Native metadata and Web signature limitations. |
| Incorrect input or output | Type, shape, length, order, or quantization mismatch | Check the model specification and [tensor guide](guide/tensors.md); do not blindly reuse example data. |
| Output not initialized | Web output read before inference completes | Write inputs, await `run()`, then read outputs. |
| GPU/NPU unavailable or slow | Unsupported plugin, device, browser, or model | Inspect logs, verify CPU first, then test on the target device. See [hardware acceleration](guide/acceleration.md). |
| Operation fails after close | Compiler or buffer already released | Adjust resource scope; do not reuse closed objects. See [lifecycle](guide/lifecycle.md). |

If the problem persists, prepare a shareable minimal reproduction and complete error details before [reporting an issue](support.md); do not upload sensitive models or data.
