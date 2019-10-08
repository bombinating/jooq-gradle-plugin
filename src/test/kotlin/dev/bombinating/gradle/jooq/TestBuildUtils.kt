package dev.bombinating.gradle.jooq

import java.io.File
import java.nio.file.Path

fun Path.createBuildFile(
    config: TestConfig,
    depBlock: String,
    body: TestConfig.() -> String
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
    config: TestConfig,
    depBlock: String,
    body: TestConfig.() -> String
) = """
    |import dev.bombinating.gradle.jooq.*
    |import org.jooq.meta.jaxb.Logging
    |
    |val genDir = "${'$'}projectDir/${config.genDir}"
    |
    |plugins {
    |    java
    |    id("dev.bombinating.jooq-codegen")
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