@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

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

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        macosArm64()
    ).forEach { nativeTarget ->
        nativeTarget.binaries.framework {
            baseName = "KmpLiteRT"
            isStatic = true
        }
    }

    linuxX64()
    mingwX64()

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
            implementation(libs.androidx.core.ktx)
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
