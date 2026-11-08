@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.vanniktech)
    id("kmplitert.native-conventions")
}

group = "io.github.leitingzi"
version = libs.versions.kmplitert.get()

base {
    archivesName.set("kmplitert-core")
}

val isPublishToMavenCentral = gradle.startParameter.taskNames.any {
    it.contains("publishToMavenCentral", ignoreCase = true)
}

mavenPublishing {
    publishToMavenCentral()

    if (isPublishToMavenCentral) {
        signAllPublications()
    }

    coordinates(groupId = group.toString(), artifactId = "core", version = version.toString())

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
        namespace = "io.github.kmplitert.core"
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
        withDeviceTestBuilder {
            sourceSetTreeName = "androidDeviceTest"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    if (isPublishToMavenCentral || LiteRT.isAppleToolchainAvailable()) {
        iosArm64()
        iosSimulatorArm64()
        macosArm64()
    }

    if (isPublishToMavenCentral || HostManager.hostIsMingw) {
        mingwX64()
    }

    if (isPublishToMavenCentral || HostManager.hostIsLinux) {
        linuxX64()
        linuxArm64()
    }

    LiteRT.configureNativeBundling(projects.core)

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
                    useFirefox()
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
                    useFirefox()
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
            implementation(npm("@litertjs/core", "2.6.0"))
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutinesCore)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
//            implementation(projects.tool)
        }
        sourceSets.named("androidDeviceTest") {
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.testExt.junit)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    testLogging {
        showStandardStreams = true
    }
}

tasks.withType<KotlinNativeTest>().configureEach {
    workingDir = rootProject.projectDir.absolutePath

    if (name == "iosSimulatorArm64Test") {
        val modelPath = layout.projectDirectory
            .file("CelsiusToFahrenheit.tflite")
            .asFile
            .absolutePath
        environment("SIMCTL_CHILD_KMPLITERT_TEST_MODEL_PATH", modelPath)
    }
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
        from("src/nativeInterop/lib/litert/$source") {
            into(target)
        }
    }

    into("src/jvmMain/resources")
}