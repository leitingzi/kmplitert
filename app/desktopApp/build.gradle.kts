import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.library.compose)
    implementation(projects.library.kmplitert)

    implementation(compose.desktop.currentOs)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.kotlinx.coroutinesSwing)
}

compose.desktop {
    application {
        mainClass = "com.leitz.kmplitert.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.leitz.kmplitert"
            packageVersion = "1.0.0"
        }
    }
}