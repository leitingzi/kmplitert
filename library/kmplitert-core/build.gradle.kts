@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(groupId = "io.github.leitingzi", artifactId = "kmplitert-core", version = "0.1")

    pom {
        name = "KmpLiteRT library"
        description = "A library for running TensorFlow Lite on KMP."
        inceptionYear = "2026"
        url = "https://github.com/leitingzi/kmplitert"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "leitingzi"
                name = "yebintang"
                url = "https://github.com/leitingzi/kmplitert"
            }
        }
        scm {
            url = "https://github.com/leitingzi/kmplitert"
            connection = "scm:git:git://github.com/leitingzi/kmplitert.git"
            developerConnection = "scm:git:ssh://git@github.com/leitingzi/kmplitert.git"
        }
    }
}

kotlin {
    androidLibrary {
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

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "KmpLiteRT"
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