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

tasks.register("buildAndroidJni") {
    group = "build"
    description = "Build Android JNI library manually"
    
    doLast {
        println("Note: Native JNI build for tool module requires manual CMake/NDK execution or future integration with AGP KMP.")
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

mavenPublishing {
    publishToMavenCentral()

    if (isPublishToMavenCentral) {
        signAllPublications()
    }

    coordinates(groupId = group.toString(), artifactId = "tool", version = version.toString())

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
        namespace = "io.github.kmplitert.tool"
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