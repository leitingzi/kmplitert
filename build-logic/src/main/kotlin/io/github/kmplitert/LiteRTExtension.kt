package io.github.kmplitert

import org.gradle.api.artifacts.ProjectDependency

interface LiteRTExtension {
    fun configureNativeBundling(coreProject: ProjectDependency)
}