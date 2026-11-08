@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("kmplitert.native-conventions")
}

kotlin {
    android {
        namespace = "org.example.kmplitert.appcore"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    if (HostManager.hostIsMac) {
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "AppCore"
                isStatic = false // Changed to false to allow better dynamic linking with LiteRT

                val targetDir = when (iosTarget.konanTarget) {
                    KonanTarget.IOS_ARM64 -> "ios/arm64"
                    KonanTarget.IOS_SIMULATOR_ARM64 -> "ios/sim-arm64"
                    else -> null
                } ?: return@framework

                val libPath = "$projectDir/src/nativeInterop/lib/litert/$targetDir"

                // Link against the LiteRT dynamic library
                linkerOpts("-L$libPath", "-lLiteRt", "-lc++")

                // Configure rpath for the system to find the dylib inside the framework
                linkerOpts("-Wl,-rpath,@executable_path/Frameworks")
                linkerOpts("-Wl,-rpath,@loader_path/Frameworks")

                // Copy the dylibs into the framework output directory using a dedicated Copy task
                val bundleTaskName = "bundleLiteRtTo${iosTarget.name.replaceFirstChar { it.uppercase() }}${name.replaceFirstChar { it.uppercase() }}"
                val bundleTask = tasks.register<Copy>(bundleTaskName) {
                    description = "Package LiteRT dynamic libraries into the framework."
                    from(libPath)
                    include("*.dylib")
                    into(linkTaskProvider.flatMap {
                        it.destinationDirectory.map { dir ->
                            dir.asFile.resolve("${baseName}.framework/Frameworks")
                        }
                    })
                }

                // Ensure Xcode assembly tasks depend on our bundling task
                tasks.matching {
                    (it.name.startsWith("assemble") || it.name.startsWith("embedAndSign")) &&
                            it.name.contains(iosTarget.name, ignoreCase = true) &&
                            it.name.contains("AppleFrameworkForXcode")
                }.configureEach {
                    dependsOn(bundleTask)
                }
            }
        }
    }

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    js {
        browser()
    }

    wasmJs {
        browser()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.uiToolingPreview)
        }
        jsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            api(projects.core)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}