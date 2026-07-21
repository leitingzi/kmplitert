package io.github.kmplitert

import org.gradle.api.Project

open class LiteRTExtensionImpl(private val project: Project) : LiteRTExtension {
    override fun configureNativeBundling(coreProjectPath: String) {
        project.configureNativeLiteRTBundling(coreProjectPath)
    }
}