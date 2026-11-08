package io.github.kmplitert

import org.gradle.api.artifacts.ProjectDependency

interface LiteRTExtension {
    fun isAppleToolchainAvailable(): Boolean

    fun configureNativeBundling(coreProject: ProjectDependency)
}