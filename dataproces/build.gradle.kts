@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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
    archivesName.set("kmplitert-dataprocess")
}

val isPublishToMavenCentral = gradle.startParameter.taskNames.any {
    it.contains("publishToMavenCentral", ignoreCase = true)
}

mavenPublishing {
    publishToMavenCentral()

    if (isPublishToMavenCentral) {
        signAllPublications()
    }

    coordinates(groupId = group.toString(), artifactId = "dataprocess", version = version.toString())

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
        namespace = "io.github.kmplitert.dataprocess"
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

    if (HostManager.hostIsMac) {
        iosArm64()
        iosSimulatorArm64()
        macosArm64()
    }

    if (HostManager.hostIsMingw) {
        mingwX64()
    }

    if (HostManager.hostIsLinux) {
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
            testTask { useKarma { useChromeHeadless() } }
        }
    }

    wasmJs {
        compilations.named("main") {
            packageJson {
                name = "kmplitert-core-wasmjs"
            }
        }
        browser {
            testTask { useKarma { useChromeHeadless() } }
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
            api(projects.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}