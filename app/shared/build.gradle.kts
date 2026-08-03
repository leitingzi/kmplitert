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
                isStatic = false // Changed too false to allow better dynamic linking with LiteRT

                // Redefine libDir mapping for LiteRT paths
                val targetDir = when (iosTarget.konanTarget) {
                    KonanTarget.IOS_ARM64 -> "ios/arm64"
                    KonanTarget.IOS_SIMULATOR_ARM64 -> "ios/sim-arm64"
                    else -> null
                } ?: return@framework

                val libPath = "$projectDir/src/nativeInterop/lib/litert/$targetDir"
                linkerOpts("-L$libPath", "-lLiteRt", "-lc++")

                // Add portable rpath for the framework itself
                linkerOpts("-Wl,-rpath,@executable_path/Frameworks")
                linkerOpts("-Wl,-rpath,@loader_path/Frameworks")

                // Copy the dylibs into the framework output directory using a dedicated task to be configuration-cache friendly
                val bundleTaskName = "bundleLiteRtTo${iosTarget.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
                val bundleTask = tasks.register<Copy>(bundleTaskName) {
                    description = "Package the dynamic library built from Litert's iOS C++ code into the framework."
                    dependsOn(linkTaskProvider)
                    from(libPath)
                    include("*.dylib")
                    into(linkTaskProvider.flatMap {
                        it.destinationDirectory.map { dir ->
                            dir.asFile.resolve("${baseName}.framework/Frameworks")
                        }
                    })
                }

                // Ensure the Xcode assembly and embedding tasks depend on our bundling task
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

    jvm()

    js {
        browser()
    }

    wasmJs {
        browser()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.material.icons.extended)

            api(projects.core)
            api(projects.tool)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}