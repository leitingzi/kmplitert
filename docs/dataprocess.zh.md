# 数据处理

Data Processing（`dataproces`）是独立的可选模块，用于准备模型输入，处理图像和音频数据；**Core** 负责加载模型并执行推理。

Core 可脱离 Data Processing 单独使用。Data Processing 仅为直接向 `TFBuffer` 写入处理结果而依赖 Core。

## 添加模块

在当前仓库中，将模块添加到共享源集：

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":dataproces"))
        }
    }
}
```

## 图像

`LiteRtImage` 支持编码图像字节、原始像素和受支持的平台帧。变换会返回新图像。写入张量前，请将图像转换为模型要求的尺寸、通道顺序和值范围：

```kotlin
import io.github.kmplitert.dataprocess.image.LiteRtImage

val image = LiteRtImage(encodedBytes)
    .centerCrop(224, 224)
    .resize(224, 224)
    .toRgb()

image.writeFloatBuffer(inputBuffer, mean = 127.5f, std = 127.5f)
```

量化输入使用 `writeInt8Buffer`。写入前核对模型的张量类型、尺寸和量化参数。

## 音频

`LiteRtAudio` 提供 WAV 解码、声道转换和重采样；`SignalProcessing` 提供 FFT 与频谱图工具：

```kotlin
import io.github.kmplitert.dataprocess.audio.LiteRtAudio

val audio = LiteRtAudio.fromWav(wavBytes)
    .toMono()
    .resample(targetSampleRate = 16_000)
```

WAV 解码器支持小端 PCM（8/16/24/32 位）和 32 位 IEEE 浮点。采样率、声道数、归一化方式和特征形状必须符合模型要求。

## 下一步

- [使用 Core 执行推理](getting-started.md)
- [查看平台支持情况](platforms.md)
- [试用浏览器在线示例](demo.md)
