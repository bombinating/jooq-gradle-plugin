val kotlinVersion: String by settings

val artifactoryPluginVersion: String by settings
val bintrayPluginVersion: String by settings
val dokkaPluginVersion: String by settings
val releasePluginVersion: String by settings
val detektPluginVersion: String by settings
val publishPluginVersion: String by settings

pluginManagement {
    plugins {
        kotlin("jvm") version kotlinVersion
        id("com.jfrog.artifactory") version artifactoryPluginVersion
        id("com.jfrog.bintray") version bintrayPluginVersion
        id("io.gitlab.arturbosch.detekt") version detektPluginVersion
        id("org.jetbrains.dokka") version dokkaPluginVersion
        id("com.gradle.plugin-publish") version publishPluginVersion
        id("net.researchgate.release") version releasePluginVersion
    }
}

rootProject.name = "jooq-gradle-plugin"

