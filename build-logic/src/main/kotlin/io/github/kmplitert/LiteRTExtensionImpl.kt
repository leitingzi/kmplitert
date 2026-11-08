package io.github.kmplitert

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.jetbrains.kotlin.konan.target.HostManager

open class LiteRTExtensionImpl(private val project: Project) : LiteRTExtension {
    override fun isAppleToolchainAvailable(): Boolean {
        if (!HostManager.hostIsMac) {
            return false
        }

        return project.providers.exec {
            commandLine("xcrun", "xcodebuild", "-version")
            isIgnoreExitValue = true
        }.result.get().exitValue == 0
    }

    override fun configureNativeBundling(coreProject: ProjectDependency) {
        project.configureNativeLiteRTBundling(project.project(coreProject.path))
    }
}