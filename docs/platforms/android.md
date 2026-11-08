# Android

`androidMain` 使用 Google LiteRT Android 运行时。当前 Core 构建配置的最低版本为 Android API 24；运行时依赖由 Core 的 Android 依赖配置提供。

## 提供模型文件

仓库的 Android 设备测试将模型从 assets 复制到可访问的文件，然后将绝对路径传入 `LiteRTCompiler`。例如先将模型放入应用的 `src/main/assets/`，再准备文件：

```kotlin
import android.content.Context
import java.io.File

fun copyModel(context: Context): File {
    val modelFile = File(context.cacheDir, "CelsiusToFahrenheit.tflite")
    context.assets.open("CelsiusToFahrenheit.tflite").use { input ->
        modelFile.outputStream().use { output -> input.copyTo(output) }
    }
    return modelFile
}
```

文件复制涉及磁盘 I/O；在应用合适的后台调度器中执行。将 `modelFile.absolutePath` 交给[推理示例](../getting-started.md)。

`LiteRTFileUtils` 属于可选 Tool 模块；仅使用 Core 时不需要初始化它。

## 硬件与验证

CPU、GPU、NPU 的选择交由 Android LiteRT 处理，实际能力取决于设备、模型和运行时。仓库包含设备端 CPU 推理测试，但现有 Core CI 没有列出该设备测试的执行步骤；请在目标设备或模拟器上验证。

若初始化失败，先检查文件是否存在、路径是否可读及模型是否完整，再排查设备和加速器兼容性。
