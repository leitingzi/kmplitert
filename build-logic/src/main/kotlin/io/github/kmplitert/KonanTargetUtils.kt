package io.github.kmplitert

import org.jetbrains.kotlin.konan.target.KonanTarget

val KonanTarget.isApple: Boolean get() = when (this) {
    KonanTarget.IOS_ARM64,
    KonanTarget.IOS_SIMULATOR_ARM64,
    KonanTarget.IOS_X64,
    KonanTarget.MACOS_ARM64 -> true
    else -> false
}

val KonanTarget.isLinux: Boolean get() = when (this) {
    KonanTarget.LINUX_ARM64,
    KonanTarget.LINUX_X64,
    KonanTarget.LINUX_ARM32_HFP -> true
    else -> false
}

val KonanTarget.libDir: String? get() = when (this) {
    KonanTarget.ANDROID_ARM64 -> "android/arm64"
    KonanTarget.ANDROID_ARM32 -> "android/arm32"
    KonanTarget.ANDROID_X64 -> "android/x86-64"
    KonanTarget.IOS_ARM64 -> "ios/arm64"
    KonanTarget.IOS_SIMULATOR_ARM64 -> "ios/sim-arm64"
    KonanTarget.MINGW_X64 -> "windows/x86-64"
    KonanTarget.LINUX_ARM64 -> "linux/arm64"
    KonanTarget.LINUX_X64 -> "linux/x86-64"
    KonanTarget.MACOS_ARM64 -> "macos/arm64"
    else -> null
}

fun String.toKonanTarget(): KonanTarget? = when (this) {
    "mingwX64" -> KonanTarget.MINGW_X64
    "linuxX64" -> KonanTarget.LINUX_X64
    "linuxArm64" -> KonanTarget.LINUX_ARM64
    "macosArm64" -> KonanTarget.MACOS_ARM64
    "iosArm64" -> KonanTarget.IOS_ARM64
    "iosSimulatorArm64" -> KonanTarget.IOS_SIMULATOR_ARM64
    "androidNativeArm64" -> KonanTarget.ANDROID_ARM64
    "androidNativeArm32" -> KonanTarget.ANDROID_ARM32
    "androidNativeX64" -> KonanTarget.ANDROID_X64
    else -> null
}