# KMPLiteRT documentation

KMPLiteRT runs LiteRT models from Kotlin Multiplatform applications. Use **Core** to load a model and run inference; add **Data Processing** only when you need image or audio preprocessing.

<div class="grid cards" markdown>

-   **Get started**

    ---

    Add Core, prepare a model, and run your first inference.

    [Installation and first inference →](getting-started.md)

-   **Core runtime**

    ---

    Learn about model lifecycle, tensor buffers, and hardware acceleration.

    [Explore Core →](guide/lifecycle.md)

-   **Data processing**

    ---

    Decode, transform, and normalize image or audio inputs.

    [Explore Data Processing →](dataprocess.md)

-   **Platform guides**

    ---

    Configure Android, JVM, Apple, Native, JavaScript, or WasmJS.

    [Choose a platform →](platforms.md)

</div>

## Two modules, separate responsibilities

| Module | What it does | Use it when |
|---|---|---|
| **Core** | Loads LiteRT models, manages tensor buffers, and runs inference. | Your app already prepares model-compatible tensors. |
| **Data Processing** (`dataproces`) | Decodes and transforms image or audio data into model-ready values. | Your app needs preprocessing before inference. |

Core can be used on its own and does not depend on Data Processing. Data Processing is optional; it depends on Core only to offer direct `TFBuffer` integration.

## Keep exploring

- [Run the interactive browser demo](demo.md)
- [Browse the Core API](core-api.md)
- [Troubleshoot common issues](troubleshooting.md)
