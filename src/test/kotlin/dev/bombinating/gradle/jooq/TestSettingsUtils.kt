package dev.bombinating.gradle.jooq

import java.io.File
import java.nio.file.Path

fun Path.createSettingsFile(projectName: String) = File(toFile(), settingsFilename).also {
    it.writeText(createSettingsContent(projectName))
}

private fun createSettingsContent(projectName: String) = """
    |rootProject.name = "$projectName"
    |pluginManagement {
    |    repositories {
    |        mavenLocal()
    |        gradlePluginPortal()
    |    }
    |}""".trimMargin("|")