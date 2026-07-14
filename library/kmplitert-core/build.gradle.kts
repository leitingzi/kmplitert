@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.gradle.api.file.DuplicatesStrategy

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech)
}

val dokkaBuild = providers.gradleProperty("dokkaBuild").isPresent

group = "io.github.leitingzi"
version = "0.1.2"

base {
    archivesName.set("kmplitert-core")
}

mavenPublishing {
    publishToMavenCentral()

    val isPublishToMavenCentral = gradle.startParameter.taskNames.any {
        it.contains("publishToMavenCentral", ignoreCase = true)
    }

    if (isPublishToMavenCentral) {
        signAllPublications()
    }

    coordinates(
        groupId = group.toString(),
        artifactId = "kmplitert-core",
        version = version.toString()
    )

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
    val isMac = HostManager.hostIsMac
    val isWindows = HostManager.hostIsMingw
    val isLinux = HostManager.hostIsLinux

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

    if (isMac) {
        iosArm64()
        iosSimulatorArm64()
        macosArm64()
    }

    if (isWindows) {
        mingwX64()
    }

    if (isLinux) {
        linuxX64()
        linuxArm64()
    }

    // android
    // androidNativeArm64()
    // androidNativeArm32()
    // androidNativeX64()

    targets.withType<KotlinNativeTarget>().configureEach {
        val basePath = "src/nativeInterop"
        compilations.getByName("main").cinterops {
            create("litert") {
                definitionFile.set(project.file("$basePath/cinterop/litert.def"))
                includeDirs(project.file("$basePath/include"))
            }
        }

        binaries.all {
            val pathDir = project.file("$basePath/lib/litert/${konanTarget.libDir}")
            linkerOpts("-L${pathDir.absolutePath}", "-lLiteRt")
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

    @OptIn(ExperimentalWasmDsl::class)
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
    val target = targetName?.toKonanTarget() ?: return@configureEach
    val libDir = project.file("src/nativeInterop/lib/litert/${target.libDir}")
    when(target) {
        KonanTarget.MINGW_X64 -> {
            val env = System.getenv("PATH")
            val list = listOfNotNull(libDir.absolutePath, env)
            environment(name = "PATH", value = list.joinToString(";"))
        }
        KonanTarget.LINUX_X64, KonanTarget.LINUX_ARM64 -> {
            val env = System.getenv("LD_LIBRARY_PATH")
            val list = listOfNotNull(libDir.absolutePath, env)
            environment(name = "LD_LIBRARY_PATH", value = list.joinToString(":"))
        }
        else -> {}
    }
}

tasks.withType<Test>().configureEach {
    testLogging {
        showStandardStreams = true
    }
}

val KonanTarget.isAppleTarget: Boolean get() = when (this) {
    KonanTarget.IOS_ARM64, KonanTarget.IOS_SIMULATOR_ARM64, KonanTarget.MACOS_ARM64 -> true
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

tasks.register<Copy>("copyNativeLitertToJvm") {
    group = "custom"
    description = "Copy native Litert lib to jvm platform resources"

    val copyList = listOf(
        "windows/x86-64" to "win32-x86-64",
        "linux/x86-64" to "linux-x86-64",
        "linux/arm64" to "linux-aarch64",
        "macos/arm64" to "darwin-aarch64",
    )

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    copyList.forEach { (source, target) ->
        from("src/nativeInterop/lib/litert/$source") {
            into(target)
        }
    }

    into("src/jvmMain/resources")
}