@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest


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
    androidLibrary {
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
        mingwX64()
    )

    nativeTargets.forEach { nativeTarget ->
        nativeTarget.binaries.all {
            val osName = when {
                nativeTarget.name.contains("mingw") -> "win32-x86-64"
                nativeTarget.name.contains("linux") -> "linux-x86-64"
                nativeTarget.name.contains("macos") || nativeTarget.name.contains("ios") -> {
                    if (nativeTarget.name.contains("Arm64")) "darwin-aarch64" else "darwin-x86-64"
                }
                else -> null
            }

            if (osName != null) {
                val libDir = project.file("src/jvmMain/resources/$osName")
                if (libDir.exists()) {
                    val libPath = libDir.absolutePath.replace("\\", "/")
                    linkerOpts("-L$libPath", "-lLiteRt")
                    if (nativeTarget.name.contains("macos") || nativeTarget.name.contains("ios")) {
                        linkerOpts("-rpath", libPath)
                    }
                }
            }
        }

        if (nativeTarget.name.contains("ios") || nativeTarget.name.contains("macos")) {
            nativeTarget.binaries.withType<Framework>().all {
                baseName = "KmpLiteRT"
                isStatic = true
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
                    useChrome()
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
                    useChrome()
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
            api(projects.library.kmplitertNative)
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
    val isMingw = targetName?.contains("mingw", ignoreCase = true) == true
    val isLinux = targetName?.contains("linux", ignoreCase = true) == true
    
    val osName = when {
        isMingw -> "win32-x86-64"
        isLinux -> "linux-x86-64"
        else -> null
    }

    if (osName == null) {
        return@configureEach
    }

    val libDir = project.file("src/jvmMain/resources/$osName")
    if (!libDir.exists()) {
        return@configureEach
    }

    val libPath = libDir.absolutePath
    when {
        isMingw -> {
            val currentPath = System.getenv("PATH")
            val newPath = if (currentPath.isNullOrEmpty()) libPath else "$libPath;$currentPath"
            environment("PATH", newPath)
        }
        isLinux -> {
            val currentLdPath = System.getenv("LD_LIBRARY_PATH")
            val newLdPath = if (currentLdPath.isNullOrEmpty()) libPath else "$libPath:$currentLdPath"
            environment("LD_LIBRARY_PATH", newLdPath)
        }
    }
}
