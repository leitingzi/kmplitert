import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(projects.app.shared)
}

compose.desktop {
    application {
        mainClass = "org.example.kmplitert.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.kmplitert"
            packageVersion = "1.0.0"
        }
    }
}