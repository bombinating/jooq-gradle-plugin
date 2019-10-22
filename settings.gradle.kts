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
val kotlinVersion: String by settings

val artifactoryPluginVersion: String by settings
val bintrayPluginVersion: String by settings
val buildScanVersion: String by settings
val dokkaPluginVersion: String by settings
val releasePluginVersion: String by settings
val detektPluginVersion: String by settings
val publishPluginVersion: String by settings

pluginManagement {
    @Suppress("UnstableApiUsage")
    plugins {
        kotlin("jvm") version kotlinVersion
        id("com.jfrog.artifactory") version artifactoryPluginVersion
        id("com.jfrog.bintray") version bintrayPluginVersion
        id("io.gitlab.arturbosch.detekt") version detektPluginVersion
        id("org.jetbrains.dokka") version dokkaPluginVersion
        id("com.gradle.plugin-publish") version publishPluginVersion
        id("net.researchgate.release") version releasePluginVersion
        id("com.gradle.build-scan") version buildScanVersion
    }
}
