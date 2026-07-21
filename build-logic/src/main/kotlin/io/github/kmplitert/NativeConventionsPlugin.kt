package io.github.kmplitert

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.KonanTarget

class NativeConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(
            LiteRTExtension::class.java,
            "LiteRT",
            LiteRTExtensionImpl::class.java,
            project
        )
    }
}

fun Project.configureNativeLiteRTBundling(coreProjectPath: String) {
    val coreProject = project(coreProjectPath)
    val kotlin = extensions.getByType<KotlinMultiplatformExtension>()
    val cInteropPath = "src/nativeInterop"

    kotlin.targets.withType<KotlinNativeTarget>().configureEach {
        if (konanTarget.isApple) {
            compilations.configureEach {
                compileTaskProvider.configure {
                    compilerOptions {
                        val ios = "appleMinos.ios_arm64=15.0"
                        val iosSimulator = "appleMinos.ios_simulator_arm64=15.0"
                        val macos = "appleMinos.macosx_arm64=12.0"
                        freeCompilerArgs.add("-Xoverride-konan-properties=$ios;$iosSimulator;$macos")
                    }
                }
            }
        }

        compilations.getByName("main").cinterops {
            create("litert") {
                definitionFile.set(coreProject.layout.projectDirectory.file("$cInteropPath/cinterop/litert.def"))
                includeDirs(coreProject.layout.projectDirectory.dir("$cInteropPath/include"))
            }
        }

        binaries.all {
            val targetDir = konanTarget.libDir ?: return@all
            val libPathFile = coreProject.layout.projectDirectory.dir("$cInteropPath/lib/litert/$targetDir")
            val path = libPathFile.asFile.absolutePath

            linkerOpts("-L$path", "-lLiteRt")

            if (konanTarget.isApple) {
                linkerOpts("-lc++")
                linkerOpts("-Wl,-rpath,@executable_path", "-Wl,-rpath,@executable_path/Frameworks")
                linkerOpts("-Wl,-rpath,@loader_path", "-Wl,-rpath,@loader_path/Frameworks")
                linkerOpts("-Wl,-rpath,@loader_path/../../Frameworks")
            } else if (konanTarget.name.contains("linux")) {
                linkerOpts("-Wl,-rpath,$path")
                linkerOpts("-Wl,--allow-shlib-undefined")
            }

            val bundleTaskName = "bundleLiteRtTo${konanTarget.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
            val isAppleTarget = konanTarget.isApple
            val bundleTask = tasks.register<Copy>(bundleTaskName) {
                from(libPathFile) {
                    include("*.dylib", "*.so", "*.dll")
                }
                into(linkTaskProvider.flatMap { it.destinationDirectory })

                if (isAppleTarget) {
                    into("Frameworks") {
                        from(libPathFile)
                        include("*.dylib")
                    }
                }
            }
            linkTaskProvider.configure {
                finalizedBy(bundleTask)
            }
        }
    }

    tasks.withType<KotlinNativeTest>().configureEach {
        val target = targetName?.toKonanTarget() ?: return@configureEach
        val targetDir = target.libDir ?: return@configureEach
        val libPathFile = coreProject.layout.projectDirectory.dir("$cInteropPath/lib/litert/$targetDir")
        val libPathAbs = libPathFile.asFile.absolutePath

        fun setEnvironment(envPath: String, sep: String) {
            val value = listOfNotNull(libPathAbs, System.getenv(envPath))
            environment(name = envPath, value = value.joinToString(separator = sep))
        }

        when(target) {
            KonanTarget.MINGW_X64 -> setEnvironment("PATH", ";")
            KonanTarget.LINUX_X64, KonanTarget.LINUX_ARM64 -> setEnvironment("LD_LIBRARY_PATH", ":")
            KonanTarget.MACOS_ARM64, KonanTarget.IOS_ARM64, KonanTarget.IOS_SIMULATOR_ARM64 -> {
                setEnvironment("DYLD_LIBRARY_PATH", ":")
                if (target == KonanTarget.IOS_SIMULATOR_ARM64) {
                    environment("SIMCTL_CHILD_DYLD_LIBRARY_PATH", libPathAbs)
                }
            }
            else -> {}
        }

        val targetPrefix = target.name.replaceFirstChar { it.uppercase() }
        dependsOn(tasks.withType<Copy>().matching { it.name.startsWith("bundleLiteRtTo$targetPrefix") })
    }
}
