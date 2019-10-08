package dev.bombinating.gradle.jooq

import java.io.File
import java.nio.file.Path

fun Path.createBuildFile(
    config: TestConfigInfo,
    depBlock: String,
    body: TestConfigInfo.() -> String
) = File(toFile(), "build.gradle.kts").also {
    it.writeText(
        createBuildContent(
            config = config,
            depBlock = depBlock,
            body = body
        )
    )
}

private fun createBuildContent(
    config: TestConfigInfo,
    depBlock: String,
    body: TestConfigInfo.() -> String
) = """
    |import dev.bombinating.gradle.jooq.*
    |import org.jooq.meta.jaxb.Logging
    |
    |val genDir = "${'$'}projectDir/${config.genDir}"
    |
    |plugins {
    |    java
    |    id("dev.bombinating.jooq-codegen") version "${config.jooqPluginVersion}"
    |}
    |
    |sourceSets["main"].java {
    |    srcDir(genDir)
    |}
    |
    |group = "com.acme"
    |version = "1.0-SNAPSHOT"
    |
    |repositories {
    |    mavenLocal()
    |    mavenCentral()
    |}
    |
    |dependencies {
    |$depBlock
    |}
    |
    |configure<JavaPluginConvention> {
    |    sourceCompatibility = ${config.javaVersion}
    |}
    |
    |${body(config)}
    """.trimMargin("|")