@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech)
}

dokka {
    dokkaSourceSets.configureEach {
        suppress.set(true)
    }

    dokkaSourceSets.named("commonMain") {
        suppress.set(false)
    }
}

group = "io.github.leitingzi"
version = libs.versions.kmplitert.get()

base {
    archivesName.set("kmplitert-tool")
}

val isPublishToMavenCentral = gradle.startParameter.taskNames.any {
    it.contains("publishToMavenCentral", ignoreCase = true)
}
val isMac = HostManager.hostIsMac
val isWindows = HostManager.hostIsMingw
val isLinux = HostManager.hostIsLinux
val cInteropPath = "../kmplitert-core/src/nativeInterop"

mavenPublishing {
    publishToMavenCentral()

    if (isPublishToMavenCentral) {
        signAllPublications()
    }

    coordinates(groupId = group.toString(), artifactId = "kmplitert-tool", version = version.toString())

    pom {
        name = "KMP LiteRT Tool"
        description = "KMPLiteRT Tool provides utility functions for image preprocessing and file handling when working with LiteRT models."
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
        namespace = "io.github.leitingzi.kmplitert.tool"
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
            isReturnDefaultValues = true
        }
    }

    if (isPublishToMavenCentral || isMac) {
        iosArm64()
        iosSimulatorArm64()
        macosArm64()
    }

    if (isPublishToMavenCentral || isWindows) {
        mingwX64()
    }

    if (isPublishToMavenCentral || isLinux) {
        linuxX64()
        linuxArm64()
    }

    targets.withType<KotlinNativeTarget>().configureEach {
        if (konanTarget.isApple) {
            compilations.configureEach {
                compileTaskProvider.configure {
                    compilerOptions {
                        val ios = "appleMinos.ios_arm64=15.0"
                        val iosSimulator = "appleMinos.ios_simulator_arm64=15.0"
                        val macos = "appleMinos.macosx_arm64=12.0"
                        freeCompilerArgs.add("-Xoverride-konan-properties=$ios;$iosSimulator;$macos")
                    }
                }
            }
        }

        binaries.all {
            val targetDir = konanTarget.libDir ?: return@all
            val coreProject = project(":library:kmplitert-core")
            val libPathFile = coreProject.layout.projectDirectory.dir("src/nativeInterop/lib/litert/$targetDir")
            val path = libPathFile.asFile.absolutePath

            // Standard link search path and library name
            linkerOpts("-L$path", "-lLiteRt")

            if (konanTarget.isApple) {
                // Link C++ standard library which is required by LiteRT
                linkerOpts("-lc++")
                // Use -Wl to ensure the compiler driver forwards these to the linker correctly
                linkerOpts("-Wl,-rpath,@loader_path")
                linkerOpts("-Wl,-rpath,$path")
            } else if (konanTarget.isLinux) {
                linkerOpts("-Wl,-rpath,$path")
            }

            if (konanTarget.isLinux) {
                linkerOpts("-Wl,--allow-shlib-undefined")
            }

            // Bundling for tests/binaries
            val bundleTaskName = "bundleLiteRtTo${konanTarget.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
            val isAppleTarget = konanTarget.isApple
            val bundleTask = tasks.register<Copy>(bundleTaskName) {
                from(libPathFile) {
                    include("*.dylib", "*.so", "*.dll")
                }
                into(linkTaskProvider.flatMap { it.destinationDirectory })

                if (isAppleTarget) {
                    into("Frameworks") {
                        from(libPathFile)
                        include("*.dylib")
                    }
                }
            }
            linkTaskProvider.configure {
                finalizedBy(bundleTask)
            }
        }
    }

    jvm()

    js {
        compilations.named("main") {
            packageJson {
                name = "kmplitert-tool-js"
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

    wasmJs {
        compilations.named("main") {
            packageJson {
                name = "kmplitert-tool-wasm"
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
            implementation(libs.androidx.core.ktx)
        }
        val androidHostTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.robolectric)
            }
        }
        webMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutinesCore)
            api(projects.library.kmplitertCore)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}

tasks.withType<KotlinNativeTest>().configureEach {
    val target = targetName?.toKonanTarget() ?: return@configureEach
    val targetDir = target.libDir ?: return@configureEach
    val coreProject = project(":library:kmplitert-core")
    val libPathFile = coreProject.layout.projectDirectory.dir("src/nativeInterop/lib/litert/$targetDir")
    val libPathAbs = libPathFile.asFile.absolutePath

    fun setEnvironment(path: String, sep: String) {
        val value = listOfNotNull(libPathAbs, System.getenv(path))
        environment(name = path, value = value.joinToString(separator = sep))
    }

    when(target) {
        KonanTarget.MINGW_X64 -> {
            setEnvironment(path = "PATH", sep = ";")
        }
        KonanTarget.LINUX_X64, KonanTarget.LINUX_ARM64 -> {
            setEnvironment(path = "LD_LIBRARY_PATH", sep = ":")
        }
        KonanTarget.MACOS_ARM64, KonanTarget.IOS_ARM64, KonanTarget.IOS_SIMULATOR_ARM64 -> {
            setEnvironment(path = "DYLD_LIBRARY_PATH", sep = ":")
        }
        else -> {}
    }

    // Fix implicit dependency: the test task uses the output of the bundle task
    val targetPrefix = target.name.replaceFirstChar { it.uppercase() }
    dependsOn(tasks.withType<Copy>().matching { it.name.startsWith("bundleLiteRtTo$targetPrefix") })
}

val KonanTarget.isApple: Boolean get() = when (this) {
    KonanTarget.IOS_ARM64,
    KonanTarget.IOS_SIMULATOR_ARM64,
    KonanTarget.IOS_X64,
    KonanTarget.MACOS_ARM64 -> true
    else -> false
}

val KonanTarget.isLinux: Boolean get() = when (this) {
    KonanTarget.LINUX_X64, KonanTarget.LINUX_ARM64, KonanTarget.LINUX_ARM32_HFP -> true
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
