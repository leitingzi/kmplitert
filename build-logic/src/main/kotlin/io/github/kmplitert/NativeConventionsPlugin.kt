package io.github.kmplitert

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBinary
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

internal fun Project.configureNativeLiteRTBundling(coreProjectPath: String) {
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
            val libPathFile = liteRtLibDir(coreProject, cInteropPath, konanTarget) ?: return@all
            val path = libPathFile.asFile.absolutePath

            linkerOpts("-L$path", "-lLiteRt")

            if (konanTarget.isApple) {
                appleLinkerOpts()
            }

            if (konanTarget.isLinux) {
                linuxLinkerOpts(path = path)
            }

            val bundleTaskName = "bundleLiteRtTo${konanTarget.name.capitalizeAscii()}${name.capitalizeAscii()}"
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
        val libPathFile = liteRtLibDir(coreProject, cInteropPath, target) ?: return@configureEach
        val libPathAbs = libPathFile.asFile.absolutePath

        fun setEnvironment(envPath: String, sep: String) {
            val value = listOfNotNull(libPathAbs, System.getenv(envPath))
            environment(name = envPath, value = value.joinToString(separator = sep))
        }

        when(target) {
            KonanTarget.MINGW_X64 -> {
                setEnvironment("PATH", ";")
            }

            KonanTarget.LINUX_X64, KonanTarget.LINUX_ARM64 -> {
                setEnvironment("LD_LIBRARY_PATH", ":")
            }

            KonanTarget.MACOS_ARM64, KonanTarget.IOS_ARM64 -> {
                setEnvironment("DYLD_LIBRARY_PATH", ":")
            }

            KonanTarget.IOS_SIMULATOR_ARM64 -> {
                environment("SIMCTL_CHILD_DYLD_LIBRARY_PATH", libPathAbs)
            }
            else -> {}
        }

        val targetPrefix = target.name.capitalizeAscii()

        val dependsOnPath = tasks.withType<Copy>().matching {
            it.name.startsWith("bundleLiteRtTo$targetPrefix")
        }

        dependsOn(dependsOnPath)
    }
}

internal fun String.capitalizeAscii(): String = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase() else it.toString()
}

internal fun liteRtLibDir(coreProject: Project, cInteropPath: String, target: KonanTarget): Directory? {
    if (target.libDir == null) {
        return null
    }

    val path = "$cInteropPath/lib/litert/${target.libDir}"
    return coreProject.layout.projectDirectory.dir(path)
}

internal fun NativeBinary.appleLinkerOpts() {
    linkerOpts("-lc++")
    linkerOpts(
        "-Wl,-rpath,@executable_path",
        "-Wl,-rpath,@executable_path/Frameworks",
        "-Wl,-rpath,@loader_path",
        "-Wl,-rpath,@loader_path/Frameworks",
        "-Wl,-rpath,@loader_path/../../Frameworks",
    )
}

internal fun NativeBinary.linuxLinkerOpts(path: String) {
    linkerOpts("-Wl,-rpath,$path", "-Wl,--allow-shlib-undefined")
}
