# Linux and Windows Native

`linuxX64`, `linuxArm64`, and `mingwX64` use the `nativeMain` C interop implementation, unlike the [JVM](jvm.md) JNA implementation. Targets are configured by host or publication conditions.

## Models and linking

Pass a readable model file path. For builds and distribution, the LiteRT main library and required plugins must match the target architecture and ABI; deploy them where the target program can load them.

The repository's Native build conventions configure linking and copying for this project. External applications must verify their own linker options and runtime search paths; do not rely on absolute paths from the build machine. On Linux, check dynamic linker search paths; on Windows, check DLLs near the executable or in the process search path.

## Verification

Core CI configures Linux x64 and Windows x64 Native tests. Linux ARM64 exists in the build configuration, but this alone does not establish its test or publication status. Test inference with the actual distribution, machine, and architecture.

Name-based tensor metadata queries currently use signature 0; buffer and inference methods accept a signature index. GPU/NPU requires compatible platform plugins and hardware.
