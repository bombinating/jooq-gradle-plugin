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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File
import java.nio.file.Path

fun jooqOsDependency(group: String, version: String) =
    """compile(group = "$group", name = "jooq", version = "$version")"""

fun dependenciesBlock(jooqDependency: String, jdbcDriverDependency: String) = """
    |   $jooqDependency
    |   compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    |   jooqRuntime($jdbcDriverDependency)
""".trimMargin("|")

fun String.packageToPath() = replace(".", "/")

fun Path.toFile(child: String) = File(toFile(), child)

fun printGradleInfo(settings: File, build: File) {
    println(
        """settings.gradle.kts:
                |
                |${settings.readText()}
                |
                |build.gradle.kts:
                |
                |${build.readText()}
                |
            """.trimMargin("|")
    )
}

fun validateGradleOutput(workspaceDir: Path, config: TestConfig, result: BuildResult, taskName: String) {
    val javaClass = workspaceDir.toFile("${config.genDir}/${config.packageName.packageToPath()}/" +
            "${if (config.addSchemaToPackage) "${config.schema}/" else "" }tables/$defaultTableName.java")
    assertTrue(javaClass.exists())
    assertTrue(result.task(":$taskName") != null)
    assertEquals(TaskOutcome.SUCCESS, result.task(":$taskName")?.outcome)
    assertTrue(javaClass.readText().contains("jOOQ version:${config.version ?: defaultJooqVersion}"))
}

fun runGradle(workspaceDir: Path, vararg args: String): BuildResult {
    val settings = File(workspaceDir.toFile(), "settings.gradle.kts")
    val build = File(workspaceDir.toFile(), "build.gradle.kts")
    printGradleInfo(settings, build)
    return GradleRunner.create()
        .withPluginClasspath()
        .withArguments(*args)
        .withProjectDir(workspaceDir.toFile())
        .forwardOutput()
        .build()
}

fun runGradleAndValidate(workspaceDir: Path, config: TestConfig, taskName: String) {
    val result = runGradle(workspaceDir, "clean", taskName, "build", "--info", "--stacktrace")
    validateGradleOutput(workspaceDir = workspaceDir, config = config, result = result, taskName = taskName)
}

fun TestConfig.basicExtensionTest(
    workspaceDir: Path,
    deps: String,
    taskName: String,
    projectName: String = defaultProjectName
) {
    workspaceDir.createPropFile()
    workspaceDir.createSettingsFile(projectName = projectName)
    workspaceDir.createBuildFile(config = this, depBlock = deps) {
        """
            |jooq {
            |   ${version?.let { """version = "$version"""" } ?: "" }
            |   ${edition?.let { "edition = ${JooqEdition::class.java.simpleName}.$edition" } ?: ""}
            |   ${basicJooqConfig()}
            |}
        """.trimMargin("|")
    }
    runGradleAndValidate(workspaceDir = workspaceDir, config = this, taskName = taskName)
}

fun TestConfig.basicTaskTest(
    workspaceDir: Path,
    deps: String,
    taskName: String,
    projectName: String = defaultProjectName
) {
    workspaceDir.createPropFile()
    workspaceDir.createSettingsFile(projectName = projectName)
    workspaceDir.createBuildFile(config = this, depBlock = deps) {
        """
            |${createJooqBlockForTask(edition = edition, version = version)}
            |tasks.register<JooqTask>("$taskName") {
            |   ${basicJooqConfig()}
            |}
        """.trimMargin("|")
    }
    runGradleAndValidate(workspaceDir = workspaceDir, config = this, taskName = taskName)
}

fun TestConfig.basicJooqConfig() = """
            |jdbc {
            |   driver = "$driver"
            |       url = "$url"
            |       user = "$username"
            |       password = "$password"
            |}
            |generator {
            |   database {
            |       $dbGenerator
            |   }
            |   target {
            |       directory = genDir
            |       packageName = "$packageName"
            |   }
            |}
            |logging = Logging.TRACE
""".trimMargin()

private fun createJooqBlockForTask(edition: JooqEdition?, version: String?): String =
    if (edition != null || version != null) {
        """
            |jooq {
            |   ${if (version != null) """version = "$version"""" else ""}
            |   ${if (edition != null) "edition = ${JooqEdition::class.java.simpleName}.$edition" else ""}
            |}
        """.trimMargin()
    } else {
        ""
    }