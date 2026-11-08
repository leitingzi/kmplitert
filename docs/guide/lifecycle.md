# Model and Inference Lifecycle

An inference consists of five stages: **prepare the model → initialize → create buffers → write and run → release resources**.

```text
modelPath → LiteRTCompiler → init()
                               ↓
                    input / output buffers
                               ↓
                      write → run → read
                               ↓
                         close resources
```

## Initialization

Create a `LiteRTCompiler` with the model path and an optional `LiteRTAccelerator`; CPU is the default. Call `init()` before querying tensors or creating buffers.

Compiler operations and read methods are `suspend` functions and must be called from a coroutine. `suspend` does not guarantee background dispatch on every platform or concurrent access to the same instance; choose scheduling appropriate to your application's threads and lifecycle.

## Query and run

Input/output ordering, names, data types, and dimensions are defined by the model. Query metadata using `getInputTensorType(name)`, `getOutputTensorType(name)`, and their buffer requirements methods. Names must come from the model; the public API does not enumerate tensor names.

Obtain input and output buffers for the same compiler and signature, write data in model order, and call `run(inputs, outputs)`. Read outputs after it completes. Multi-signature models require attention to [platform differences](../platforms.md#differences).

## Cleanup {#cleanup}

Use `try/finally` for the compiler and acquired buffers as shown in the [complete example](../getting-started.md#inference). It closes buffers before the compiler, following the Native test; the JVM compiler also releases buffers it tracks when closed. The current Android and Web buffer wrappers inherit the interface's no-op `close()`.

Do not use a buffer or compiler after closing it, or assume repeated initialization, repeated closing, and concurrent access behave identically on every platform.

## Repeated inference

While the model and resources remain valid, update inputs and call `run` again as required by the model. If the model, data type, or signature changes, do not assume earlier buffers are compatible; check the target platform and model requirements again.
