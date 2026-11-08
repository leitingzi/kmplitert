# 图像与音频处理流程

`dataproces` 是 Core 的可选配套模块。它将常用预处理保留在模型附近：解码数据、确定性变换、创建类型化数组，再写入 `TFBuffer`。

## 添加模块

该模块依赖 `:core`。在当前仓库中，将其加入共享源集：

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":dataproces"))
        }
    }
}
```

当模型输入为像素或音频采样时使用它；如果应用已拥有形状正确的张量，直接依赖 Core 即可。

## 图像流程

`LiteRtImage` 存储行优先、交错排列的灰度、RGB 或 RGBA 像素。每次变换都会返回新图像，因此每一步都清晰且可安全复用。

```kotlin
import io.github.kmplitert.dataprocess.image.LiteRtImage

val image = LiteRtImage(encodedBytes)
    .centerCrop(224, 224)
    .resize(224, 224)
    .toRgb()

image.writeFloatBuffer(
    buffer = inputBuffer,
    mean = 127.5f,
    std = 127.5f,
)
```

最后一步会将归一化后的 `FloatArray` 直接写入 Core 缓冲区。量化模型请使用 `writeInt8Buffer`。写入前务必核对模型尺寸、通道顺序、量化参数和张量类型。

| 需求 | API |
|---|---|
| 编码图像字节 | `LiteRtImage(bytes)` 或 `LiteRtImage.fromBytes(bytes)` |
| 原始 RGB 字节 | `LiteRtImage.fromRawRgb(data, width, height)` |
| 相机或平台帧 | `LiteRtImage.fromVideoFrame(frame, rotation, flip)` |
| 几何变换 | `resize`、`crop`、`centerCrop`、`rotate`、`flip` |
| 通道转换 | `toGrayscale`、`toRgb` |
| 张量值 | `toFloatArray`、`toInt8Array`、`writeFloatBuffer`、`writeInt8Buffer` |

编码图像与平台帧支持随目标平台而变化。API 会验证尺寸和原始像素长度；无效输入会尽早失败，不会生成错误张量。

## 音频流程

`LiteRtAudio` 保存交错排列的浮点采样。解码 WAV 文件后，使声道数和采样率与模型一致，再生成模型需要的特征表示。

```kotlin
import io.github.kmplitert.dataprocess.audio.LiteRtAudio
import io.github.kmplitert.dataprocess.audio.SignalProcessing
import io.github.kmplitert.dataprocess.audio.fromWav

val audio = LiteRtAudio.fromWav(wavBytes)
    .toMono()
    .resample(targetSampleRate = 16_000)

val melFrames = SignalProcessing.melSpectrogram(
    signal = audio.data,
    sampleRate = audio.sampleRate,
    fftSize = 512,
    hopSize = 160,
    numMels = 40,
)
```

`WavDecoder` 支持小端 PCM WAV（8/16/24/32 位）与 32 位 IEEE 浮点 WAV，并会拒绝损坏的头部和不受支持的格式。`SignalProcessing` 提供 FFT、Hanning/Hamming 窗、Mel 转换与滤波器组、功率频谱图和 Mel 频谱图。FFT 输入长度必须是 2 的幂。

!!! tip "将模型约束显式写出"
    预处理流程是模型契约的一部分。请将采样率、裁剪策略、归一化常量、张量形状和输出展平方式与模型集成代码放在一起。

## 在线运行

[在线示例](demo.md)会在浏览器中执行 LiteRT 模型。你可用它查看模型输入输出类型、试用内置模型，或加载本地 `.tflite` 文件，再集成到应用中。
