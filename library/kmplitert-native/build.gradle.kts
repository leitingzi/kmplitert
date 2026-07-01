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
    // Only add native targets for cinterop
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    linuxX64()
    mingwX64()

    targets.withType<KotlinNativeTarget>().forEach { nativeTarget ->
        nativeTarget.compilations.getByName("main").cinterops {
            create("litert") {
                definitionFile.set(project.file("src/nativeInterop/cinterop/litert.def"))
            }
        }
    }
}
