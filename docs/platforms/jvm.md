# JVM

`jvmMain` calls the LiteRT C API through JNA. JVM and Kotlin/Native desktop are different build targets with different dependency loading.

## Model file

Place the model in a readable local file and pass its absolute path to the [inference example](../getting-started.md). If the model is packaged in a classpath or JAR, copy the resource to a file first; a classpath resource name is not a filesystem path.

## Native libraries

The current repository's JVM resources include native libraries for Windows x86-64, Linux x86-64 / ARM64, and macOS ARM64. The loader extracts and loads the library for the current platform. Check whether the published artifact for your version contains the required binary.

Alternatively, specify the main native library file or its directory with a JVM system property:

```text
-Dkmplitert.native.path=/absolute/path/to/litert-libraries
```

The main library, optional accelerator plugins, and their dependencies must match the OS, architecture, and LiteRT ABI. Main-library loading failures raise an error; unavailable accelerator plugins produce a warning and GPU requests may fall back to CPU. Set this property before the runtime is first loaded.

## Lifecycle and tests

The JVM implementation serializes compiler operations and releases tracked buffers when closed; do not generalize this behavior to other platforms. The repository provides JVM temperature model and ABI tests, but Core CI does not list JVM test execution.

See [troubleshooting](../troubleshooting.md) for loading problems.
