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
    archivesName.set("kmplitert-core")
}

val isPublishToMavenCentral = gradle.startParameter.taskNames.any {
    it.contains("publishToMavenCentral", ignoreCase = true)
}
val isMac = HostManager.hostIsMac
val isWindows = HostManager.hostIsMingw
val isLinux = HostManager.hostIsLinux
val cInteropPath = "src/nativeInterop"

mavenPublishing {
    publishToMavenCentral()

    if (isPublishToMavenCentral) {
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
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
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

    // android
    // androidNativeArm64()
    // androidNativeArm32()
    // androidNativeX64()

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

        compilations.getByName("main").cinterops {
            create("litert") {
                definitionFile.set(layout.projectDirectory.file("$cInteropPath/cinterop/litert.def"))
                includeDirs(layout.projectDirectory.dir("$cInteropPath/include"))
            }
        }

        binaries.all {
            val targetDir = konanTarget.libDir ?: return@all
            val libPathFile = layout.projectDirectory.dir("$cInteropPath/lib/litert/$targetDir")
            val path = libPathFile.asFile.absolutePath
            
            // Standard link search path and library name
            linkerOpts("-L$path", "-lLiteRt")
            
            if (konanTarget.isApple) {
                // Link C++ standard library which is required by LiteRT
                linkerOpts("-lc++")
                // Use portable rpath settings for iOS/MacOS
                linkerOpts("-Wl,-rpath,@executable_path", "-Wl,-rpath,@executable_path/Frameworks")
                linkerOpts("-Wl,-rpath,@loader_path", "-Wl,-rpath,@loader_path/Frameworks")
                linkerOpts("-Wl,-rpath,@loader_path/../../Frameworks") // common location for tests
            } else if (konanTarget.isLinux) {
                linkerOpts("-Wl,-rpath,$path")
            }
            
            if (konanTarget.isLinux) {
                linkerOpts("-Wl,--allow-shlib-undefined")
            }

            // Robust bundling: copy dylibs to the binary destination folder and a Frameworks subfolder
            // Use a dedicated task to be configuration-cache friendly
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
            implementation(projects.library.kmplitertTool)
        }
    }
}

tasks.withType<KotlinNativeTest>().configureEach {
    val target = targetName?.toKonanTarget() ?: return@configureEach
    val targetDir = target.libDir ?: return@configureEach
    val libPathFile = layout.projectDirectory.dir("$cInteropPath/lib/litert/$targetDir")
    val libPathAbs = libPathFile.asFile.absolutePath

    // Extract bundling to a dedicated task to be configuration-cache friendly
    val prepareTestTaskName = "prepareLiteRtFor${name.replaceFirstChar { it.uppercase() }}"
    val isAppleTarget = target.isApple
    val prepareTestTask = tasks.register<Copy>(prepareTestTaskName) {
        from(libPathFile) {
            include("*.dylib", "*.so", "*.dll")
        }
        into(executable.parentFile)
        
        if (isAppleTarget) {
            into("Frameworks") {
                from(libPathFile)
                include("*.dylib")
            }
        }
    }
    
    dependsOn(prepareTestTask)

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
            // For iOS Simulator, simctl needs SIMCTL_CHILD_ prefix to propagate env vars
            if (target == KonanTarget.IOS_SIMULATOR_ARM64) {
                environment("SIMCTL_CHILD_DYLD_LIBRARY_PATH", libPathAbs)
            }
        }
        else -> {}
    }
}

tasks.withType<Test>().configureEach {
    testLogging {
        showStandardStreams = true
    }
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

tasks.register<Copy>("copyNativeLitertToJvm") {
    group = "custom"
    description = "Copy native Litert lib to jvm platform resources"

    val copyList = listOf(
        "windows/x86-64" to "win32-x86-64", "linux/x86-64" to "linux-x86-64",
        "linux/arm64" to "linux-aarch64", "macos/arm64" to "darwin-aarch64",
    )

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    copyList.forEach { (source, target) ->
        from("$cInteropPath/lib/litert/$source") {
            into(target)
        }
    }

    into("src/jvmMain/resources")
}