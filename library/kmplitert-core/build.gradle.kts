@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.HostManager


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech)
}

group = "io.github.leitingzi"
version = "0.1.1"

base {
    archivesName.set("kmplitert-core")
}

mavenPublishing {
    publishToMavenCentral()

    if (gradle.startParameter.taskNames.any {
        it.contains("publishToMavenCentral", ignoreCase = true)
    }) {
        signAllPublications()
    }

    coordinates(groupId = group.toString(), artifactId = "kmplitert-core", version = version.toString())

    pom {
        name = "KMP LiteRT"
        description = "KMPLiteRT is a Kotlin Multiplatform library for running TensorFlow Lite (LiteRT) models on Android, iOS, JVM, Native, and Web. It provides a unified, type-safe API for loading models, preparing tensors, and executing inference with consistent behavior across all supported platforms."
        inceptionYear = "2026"
        url = "https://github.com/leitingzi/kmplitert"
        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "leitingzi"
                name = "yebintang"
                url = "https://github.com/leitingzi"
                email = "553387747@qq.com"
            }
        }
        scm {
            url = "https://github.com/leitingzi/kmplitert"
            connection = "scm:git:https://github.com/leitingzi/kmplitert.git"
            developerConnection = "scm:git:ssh://git@github.com:leitingzi/kmplitert.git"
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}

kotlin {
    android {
        namespace = "io.github.leitingzi.kmplitert.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    val nativeTargets = listOf(
        iosArm64(),
        iosSimulatorArm64(),
        macosArm64(),
        linuxX64(),
        linuxArm64(),
        mingwX64(),
        androidNativeArm64(),
        androidNativeX64()
    )

//    val nativeTargets = buildList {
//        add(linuxX64())
//        add(linuxArm64())
//        add(mingwX64())
//        add(androidNativeArm64())
//        add(androidNativeX64())
//
//        if (HostManager.hostIsMac) {
//            add(iosArm64())
//            add(iosSimulatorArm64())
//            add(macosArm64())
//        }
//    }

    nativeTargets.forEach { nativeTarget ->

        val konanTarget = nativeTarget.konanTarget
        if (!HostManager.hostIsMac && (konanTarget.isAppleTarget)) {
            return@forEach
        }

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
                    else -> return@create
                }

                // Points to the resources folder in kmplitert-core
                val libDir = project.file("src/jvmMain/resources/$osName")
                if (!libDir.exists()) {
                    return@create
                }

                val libPath = libDir.absolutePath.replace("\\", "/")
                linkerOpts("-L$libPath", "-lLiteRt")
            }
        }

        nativeTarget.binaries.all {

            val osName = when (konanTarget) {
                KonanTarget.ANDROID_ARM64 -> "android_arm64"
                KonanTarget.ANDROID_X86 -> "android_x86_64"
                KonanTarget.IOS_SIMULATOR_ARM64 -> "ios_sim_arm64"
                KonanTarget.IOS_ARM64 -> "ios_arm64"
                KonanTarget.MINGW_X64 -> "win32-x86-64"
                KonanTarget.LINUX_ARM64 -> "linux-aarch64"
                KonanTarget.LINUX_X64 -> "linux-x86-64"
                KonanTarget.MACOS_ARM64 -> "darwin-aarch64"
                else -> return@all
            }

            val libDir = project.file("src/jvmMain/resources/$osName")
            if (!libDir.exists()) {
                return@all
            }

            val libPath = libDir.absolutePath.replace("\\", "/")
            linkerOpts("-L$libPath", "-lLiteRt")

            when (konanTarget) {
                KonanTarget.ANDROID_ARM64, KonanTarget.ANDROID_X86 -> {
                    linkerOpts("-lLiteRtClGlAccelerator")
                }

                KonanTarget.IOS_ARM64, KonanTarget.IOS_SIMULATOR_ARM64, KonanTarget.MACOS_ARM64 -> {
                    linkerOpts("-lLiteRtMetalAccelerator")
                }

                KonanTarget.MINGW_X64 -> {
                    linkerOpts("-lLiteRtWebGpuAccelerator")
                }

                KonanTarget.LINUX_ARM64, KonanTarget.LINUX_X64 -> {
                    linkerOpts("-lLiteRtWebGpuAccelerator", "-Wl,--allow-shlib-undefined")
                }

                else -> {}
            }

            when (konanTarget) {
                KonanTarget.IOS_ARM64, KonanTarget.IOS_SIMULATOR_ARM64, KonanTarget.MACOS_ARM64 -> {
                    linkerOpts("-rpath", libPath)
                }

                else -> {}
            }
        }

        when (konanTarget) {
            KonanTarget.IOS_ARM64, KonanTarget.IOS_SIMULATOR_ARM64, KonanTarget.MACOS_ARM64 -> {
                if (HostManager.hostIsMac) {
                    nativeTarget.binaries.withType<Framework>().all {
                        baseName = "KmpLiteRT"
                        isStatic = true
                    }
                }
            }

            else -> {}
        }
    }

    jvm()

    js {
        compilations.named("main") {
            packageJson {
                name = "kmplitert-core-js"
            }
        }
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        compilations.named("main") {
            packageJson {
                name = "kmplitert-core-wasm"
            }
        }
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.edge.litert)
        }

        jvmMain.dependencies {
            implementation(libs.java.jna)
            implementation(libs.java.jna.platform)
        }

        nativeMain.dependencies {

        }

        webMain.dependencies {
            implementation(libs.kotlinx.browser)
            implementation(npm("@litertjs/core", "2.5.2"))
        }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutinesCore)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}

tasks.withType<KotlinNativeTest>().configureEach {

    val target = targetName?.toKonanTarget()

    val osName = when (target) {
        KonanTarget.MINGW_X64 -> "win32-x86-64"
        KonanTarget.LINUX_X64 -> "linux-x86-64"
        KonanTarget.LINUX_ARM64 -> "linux-aarch64"
        else -> return@configureEach
    }

    val libDir = project.file("src/jvmMain/resources/$osName")
    if (!libDir.exists()) {
        return@configureEach
    }

    val libPath = libDir.absolutePath

    when(target) {
        KonanTarget.MINGW_X64 -> {
            val currentPath = System.getenv("PATH")
            val newPath = if (currentPath.isNullOrEmpty()) libPath else "$libPath;$currentPath"
            environment("PATH", newPath)
        }
        KonanTarget.LINUX_X64, KonanTarget.LINUX_ARM64 -> {
            val currentLdPath = System.getenv("LD_LIBRARY_PATH")
            val newLdPath = if (currentLdPath.isNullOrEmpty()) libPath else "$libPath:$currentLdPath"
            environment("LD_LIBRARY_PATH", newLdPath)
        }

        else -> {}
    }
}

fun String.toKonanTarget(): KonanTarget? = when (this) {
    "mingwX64" -> KonanTarget.MINGW_X64
    "linuxX64" -> KonanTarget.LINUX_X64
    "linuxArm64" -> KonanTarget.LINUX_ARM64
    "macosArm64" -> KonanTarget.MACOS_ARM64
    "iosArm64" -> KonanTarget.IOS_ARM64
    "iosSimulatorArm64" -> KonanTarget.IOS_SIMULATOR_ARM64
    "androidNativeX64" -> KonanTarget.ANDROID_X64
    "androidNativeArm64" -> KonanTarget.ANDROID_ARM64
    else -> null
}

tasks.withType<Test>().configureEach {
    testLogging {
        showStandardStreams = true
    }
}

val KonanTarget.isAppleTarget: Boolean
    get() = when (this) {
        KonanTarget.IOS_ARM64,
        KonanTarget.IOS_SIMULATOR_ARM64,
        KonanTarget.MACOS_ARM64 -> true
        else -> false
    }
