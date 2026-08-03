@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech)
    id("kmplitert.native-conventions")
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
    }

    if (isPublishToMavenCentral || HostManager.hostIsMac) {
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

    androidNativeArm64()
    androidNativeX64()

    LiteRT.configureNativeBundling(":core")

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
        val nativeMain = sourceSets.maybeCreate("nativeMain").apply {
            dependsOn(commonMain.get())
        }
        val nativeTest = sourceSets.maybeCreate("nativeTest").apply {
            dependsOn(commonTest.get())
        }
        val androidNativeMain by creating {
            dependsOn(nativeMain)
        }
        val androidNativeTest by creating {
            dependsOn(nativeTest)
        }
        androidNativeArm64Main.get().dependsOn(androidNativeMain)
        androidNativeArm64Test.get().dependsOn(androidNativeTest)
        androidNativeX64Main.get().dependsOn(androidNativeMain)
        androidNativeX64Test.get().dependsOn(androidNativeTest)

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
            implementation(projects.tool)
        }
    }
}

tasks.withType<Test>().configureEach {
    testLogging {
        showStandardStreams = true
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