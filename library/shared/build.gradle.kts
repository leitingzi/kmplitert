@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.dokka)
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.projectDirectory.dir("dokka"))
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    js {
        browser {
            testTask {
                useKarma {
                    useChrome()
                }
            }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useChrome()
                }
            }
        }
    }

    androidLibrary {
        namespace = "com.leitz.kmplitert.shared"
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

    swiftPMDependencies {
        iosMinimumDeploymentTarget = "26.3"

        swiftPackage(
            url = url("https://github.com/google-ai-edge/litert"),
            version = branch("main"),
            products = listOf(product("LiteRT")),
        )
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.litert)
        }
        jvmMain.dependencies {
            implementation(libs.jna)
            implementation(libs.jna.platform)
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