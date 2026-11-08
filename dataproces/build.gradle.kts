@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    id("kmplitert.native-conventions")
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

    LiteRT.configureNativeBundling(":core")

    jvm()

    js {
        browser {
            testTask { useKarma { useChromeHeadless() } }
        }
    }

    wasmJs {
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