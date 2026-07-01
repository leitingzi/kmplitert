@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech)
}

group = "io.github.leitingzi"
version = "0.1.1"

base {
    archivesName.set("kmplitert-native")
}

mavenPublishing {
    publishToMavenCentral()
    coordinates(groupId = group.toString(), artifactId = "kmplitert-native", version = version.toString())
    pom {
        name = "KmpLiteRT Native"
        description = "Low-level C Interop bindings for LiteRT."
    }
}

kotlin {
    iosArm64()
    iosSimulatorArm64()
    macosArm64()
    linuxX64()
    mingwX64()

    targets.withType<KotlinNativeTarget>().forEach { nativeTarget ->
        nativeTarget.compilations.getByName("main").cinterops {
            create("litert") {
                definitionFile.set(project.file("src/nativeInterop/cinterop/litert.def"))

                val osName = when {
                    nativeTarget.name.contains("mingw") -> "win32-x86-64"
                    nativeTarget.name.contains("linux") -> "linux-x86-64"
                    nativeTarget.name.contains("macos") || nativeTarget.name.contains("ios") -> {
                        if (nativeTarget.name.contains("Arm64")) "darwin-aarch64" else "darwin-x86-64"
                    }
                    else -> null
                }

                if (osName != null) {
                    // Points to the resources folder in kmplitert-core
                    val libDir = rootProject.project(":library:kmplitert-core").file("src/jvmMain/resources/$osName")
                    if (libDir.exists()) {
                        val libPath = libDir.absolutePath.replace("\\", "/")
                        linkerOpts("-L$libPath", "-lLiteRt")

                        // For macOS/iOS, you may need to set rpath to ensure the dynamic library can be found at runtime.
                        if (nativeTarget.name.contains("macos") || nativeTarget.name.contains("ios")) {
                            linkerOpts("-rpath", libPath)
                        }
                    }
                }
            }
        }
    }
}
