@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech)
}

group = "io.github.leitingzi"
version = "0.1.1"

base {
    archivesName.set("kmplitert-native")
}

mavenPublishing {
    publishToMavenCentral()
    coordinates(groupId = group.toString(), artifactId = "kmplitert-native", version = version.toString())
    pom {
        name = "KmpLiteRT Native"
        description = "Low-level C Interop bindings for LiteRT."
    }
}

kotlin {
    iosArm64()
    iosSimulatorArm64()
    macosArm64()
    macosX64()
    linuxX64()
    linuxArm64()
    mingwX64()
    androidNativeArm64()
    androidNativeX64()

    targets.withType<KotlinNativeTarget>().forEach { nativeTarget ->
        nativeTarget.compilations.getByName("main").cinterops {
            create("litert") {
                definitionFile.set(project.file("src/nativeInterop/cinterop/litert.def"))

                val konanTarget = nativeTarget.konanTarget

                val osName = when(konanTarget) {
                    KonanTarget.ANDROID_ARM64 -> "android_arm64"
                    KonanTarget.ANDROID_X64 -> "android_x86_64"
                    KonanTarget.IOS_ARM64 -> "ios_arm64"
                    KonanTarget.IOS_SIMULATOR_ARM64 -> "ios_sim_arm64"
                    KonanTarget.MINGW_X64 -> "win32-x86-64"
                    KonanTarget.LINUX_ARM64 -> "linux-aarch64"
                    KonanTarget.LINUX_X64 -> "linux-x86-64"
                    KonanTarget.MACOS_ARM64 -> "darwin-aarch64"
                    KonanTarget.MACOS_X64 -> "darwin-x86-64"
                    else -> return@create
                }

                // Points to the resources folder in kmplitert-core
                val libDir = rootProject.project(":library:kmplitert-core").file("src/jvmMain/resources/$osName")
                if (!libDir.exists()) {
                    return@create
                }

                val libPath = libDir.absolutePath.replace("\\", "/")
                linkerOpts("-L$libPath", "-lLiteRt")

                // For macOS/iOS, you may need to set rpath to ensure the dynamic library can be found at runtime.
                if (konanTarget == KonanTarget.MACOS_ARM64 || konanTarget== KonanTarget.MACOS_X64 ||
                    konanTarget== KonanTarget.IOS_ARM64 || konanTarget == KonanTarget.IOS_SIMULATOR_ARM64
                ) {
                    linkerOpts("-rpath", libPath)
                }
            }
        }
    }
}
