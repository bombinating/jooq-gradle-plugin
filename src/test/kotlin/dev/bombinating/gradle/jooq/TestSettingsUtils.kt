/*
 * Copyright 2019 Andrew Geery
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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