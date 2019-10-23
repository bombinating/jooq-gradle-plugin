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

fun Path.createGroovyBuildFile(
    config: TestConfig,
    depBlock: String,
    body: TestConfig.() -> String
) = File(toFile(), "build.gradle").also {
    it.writeText(
        createGroovyBuildContent(
            config = config,
            depBlock = depBlock,
            body = body
        )
    )
}

private fun createGroovyBuildContent(
    config: TestConfig,
    depBlock: String,
    body: TestConfig.() -> String
) = """
    |import dev.bombinating.gradle.jooq.*
    |import org.jooq.meta.jaxb.Logging
    |
    |plugins {
    |   id 'java'
    |   id 'dev.bombinating.jooq-codegen'
    |}
    |
    |def genDir = "${'$'}projectDir/${config.genDir}"
    |
    |sourceSets["main"].java {
    |   srcDirs(genDir)
    |}
    |
    |group = "com.acme"
    |version = "1.0-SNAPSHOT"
    |
    |repositories {
    |   mavenLocal()
    |   mavenCentral()
    |   ${createGroovyJooqRepo()?.let { it } ?: ""}
    |}
    |
    |dependencies {
    |$depBlock
    |}
    |
    |${body(config)}
    """.trimMargin("|")

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
    |    ${createJooqRepo()?.let { it } ?: ""} 
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

fun createJooqRepo(): String? {
    val jooqRepoUrl = System.getenv(envVarJooqRepoUrl)
    val jooqRepoUsername = System.getenv(envVarJooqRepoUsername)
    val jooqRepoPassword = System.getenv(envVarJooqRepoPassword)
    return if (jooqRepoUrl != null && jooqRepoPassword != null && jooqRepoUsername != null) {
        """
            |maven {
            |   url = uri(System.getenv("$envVarJooqRepoUrl"))
            |   credentials {
            |       username = System.getenv("$envVarJooqRepoUsername") 
            |       password = System.getenv("$envVarJooqRepoPassword")
            |   }
            |}
        """.trimMargin("|")
    } else {
        null
    }
}

fun createGroovyJooqRepo(): String? {
    val jooqRepoUrl = System.getenv(envVarJooqRepoUrl)
    val jooqRepoUsername = System.getenv(envVarJooqRepoUsername)
    val jooqRepoPassword = System.getenv(envVarJooqRepoPassword)
    return if (jooqRepoUrl != null && jooqRepoPassword != null && jooqRepoUsername != null) {
        """
            |maven {
            |   url "System.getenv("$envVarJooqRepoUrl")"
            |   credentials {
            |       username "System.getenv("$envVarJooqRepoUsername")" 
            |       password "System.getenv("$envVarJooqRepoPassword")"
            |   }
            |}
        """.trimMargin("|")
    } else {
        null
    }
}