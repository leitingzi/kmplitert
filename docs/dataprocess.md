# Data Processing

Data Processing (`dataproces`) is a separate, optional module for preparing model inputs. It handles image and audio data; **Core** remains responsible for model loading and inference.

Core can be used without Data Processing. Data Processing depends on Core only to write prepared values directly to `TFBuffer`.

## Add the module

In this repository, add it to your shared source set:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":dataproces"))
        }
    }
}
```

## Images

`LiteRtImage` supports encoded image bytes, raw pixels, and supported platform frames. Transformations return a new image. Convert it to the model's expected size, channel order, and value range before writing it to a tensor:

```kotlin
import io.github.kmplitert.dataprocess.image.LiteRtImage

val image = LiteRtImage(encodedBytes)
    .centerCrop(224, 224)
    .resize(224, 224)
    .toRgb()

image.writeFloatBuffer(inputBuffer, mean = 127.5f, std = 127.5f)
```

For quantized inputs, use `writeInt8Buffer`. Check the model's tensor type, dimensions, and quantization parameters before writing.

## Audio

`LiteRtAudio` provides WAV decoding, channel conversion, and resampling. `SignalProcessing` includes FFT and spectrogram helpers:

```kotlin
import io.github.kmplitert.dataprocess.audio.LiteRtAudio

val audio = LiteRtAudio.fromWav(wavBytes)
    .toMono()
    .resample(targetSampleRate = 16_000)
```

The WAV decoder accepts little-endian PCM (8/16/24/32-bit) and 32-bit IEEE float. The sample rate, channel count, normalization, and feature shape must match the model.

## Next steps

- [Run inference with Core](getting-started.md)
- [Check platform-specific support](platforms.md)
- [Try the browser demo](demo.md)
