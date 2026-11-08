# Image and Audio Pipelines

`dataproces` is the optional companion to Core. It keeps common preprocessing close to the model: decode data, transform it deterministically, create a typed array, and write it into a `TFBuffer`.

## Add the module

The module depends on `:core`. In this repository, add it to the shared source set:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":dataproces"))
        }
    }
}
```

Use it when your model accepts pixels or audio samples. Core remains the right dependency when your application already owns correctly shaped tensors.

## Image pipeline

`LiteRtImage` stores row-major, interleaved grayscale, RGB, or RGBA pixels. Its transforms return a new image, so each stage is explicit and safe to reuse.

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

The final call writes normalized `FloatArray` data directly to the Core buffer. For quantized models, use `writeInt8Buffer`. Verify the model's dimensions, channel order, quantization, and tensor type before writing.

| Need | API |
|---|---|
| Encoded image bytes | `LiteRtImage(bytes)` or `LiteRtImage.fromBytes(bytes)` |
| Raw RGB bytes | `LiteRtImage.fromRawRgb(data, width, height)` |
| Camera or platform frame | `LiteRtImage.fromVideoFrame(frame, rotation, flip)` |
| Geometry | `resize`, `crop`, `centerCrop`, `rotate`, `flip` |
| Channels | `toGrayscale`, `toRgb` |
| Tensor values | `toFloatArray`, `toInt8Array`, `writeFloatBuffer`, `writeInt8Buffer` |

Encoded-image and platform-frame support is target-specific. The API validates dimensions and raw pixel length; invalid inputs fail early rather than producing a malformed tensor.

## Audio pipeline

`LiteRtAudio` holds interleaved floating-point samples. Decode a WAV file, make its channel and sample rate match the model, then derive the feature representation your model requires.

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

`WavDecoder` accepts little-endian PCM WAV (8/16/24/32-bit) and 32-bit IEEE float WAV. It rejects malformed headers and unsupported formats. `SignalProcessing` provides FFT, Hanning/Hamming windows, Mel conversion and filter banks, power spectrograms, and Mel spectrograms. FFT input lengths must be powers of two.

!!! tip "Keep model contracts visible"
    A preprocessing pipeline is part of the model contract. Keep its sample rate, crop policy, normalization constants, tensor shape, and output flattening alongside the model integration.

## Run it online

The [interactive demo](demo.md) executes a LiteRT model in the browser. Use it to inspect a model's input and output types, try a built-in model, or load a local `.tflite` file before integrating it into an application.
