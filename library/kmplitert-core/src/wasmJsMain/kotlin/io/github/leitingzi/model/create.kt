@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.leitingzi.model

fun createCpuOptions(numThreads: Int = 4): CpuOptions =
    js("({ numThreads: numThreads })")

fun createLiteRtGpuOptions(precision: String = "fp16"): LiteRtGpuOptions =
    js("({ precision: precision })")

fun createLiteRtWebNNOptions(
    devicePreference: String = "cpu",
    powerPreference: String = "default",
    precision: String? = "fp16",
): LiteRtWebNNOptions =
    js("({ " +
            "devicePreference: devicePreference, " +
            "powerPreference: powerPreference, " +
            "precision: precision })")

fun createCompileOptions(
    environment: Environment? = null,
    cpuOptions: CpuOptions? = null,
    gpuOptions: LiteRtGpuOptions? = null,
    webNNOptions: LiteRtWebNNOptions? = null,
): CompileOptions =
    js("({ " +
            "environment: environment, " +
            "cpuOptions: cpuOptions, " +
            "gpuOptions: gpuOptions, " +
            "webNNOptions: webNNOptions })")

fun createLiteRtCompileOptions(
    accelerator: String? = null,
    gpuOptions: LiteRtGpuOptions? = null,
    webNNOptions: LiteRtWebNNOptions? = null,
): LiteRtCompileOptions =
    js("({ " +
        "accelerator: accelerator, " +
        "gpuOptions: gpuOptions, " +
        "webNNOptions: webNNOptions })")