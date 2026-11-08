package io.github.kmplitert

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

open class LiteRTExtensionImpl(private val project: Project) : LiteRTExtension {
    override fun configureNativeBundling(coreProject: ProjectDependency) {
        project.configureNativeLiteRTBundling(project.project(coreProject.path))
    }
}