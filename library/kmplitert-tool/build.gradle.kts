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
