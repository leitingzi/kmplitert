# KMPLiteRT

Run [LiteRT](https://ai.google.dev/edge/litert) models from Kotlin Multiplatform applications with a shared Kotlin API.

[Documentation](https://leitingzi.github.io/kmplitert/) · [简体中文](https://leitingzi.github.io/kmplitert/zh/) · [Source code](https://github.com/leitingzi/kmplitert)

## Modules

| Module | Responsibility |
|---|---|
| `core` | Load LiteRT models, manage typed tensor buffers, and run inference. |
| `dataproces` | Prepare image and audio inputs for models. This optional module depends on Core for direct tensor-buffer integration. |

Use Core alone when your application already prepares model-compatible tensors. Add `dataproces` when you need preprocessing; Core does not depend on it.

## Quick start

When working from this repository, add the modules you need to your KMP application's `commonMain`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            // Optional: image and audio preprocessing
            implementation(project(":dataproces"))
        }
    }
}
```

Then follow the [installation and first inference guide](https://leitingzi.github.io/kmplitert/getting-started/). For platform setup and model requirements, see the [platform guides](https://leitingzi.github.io/kmplitert/platforms/).

## Build

```shell
./gradlew :core:build
```

See the [contribution guide](https://leitingzi.github.io/kmplitert/support/) for development and support information.

## License

KMPLiteRT is distributed under the [Apache License 2.0](LICENSE).
