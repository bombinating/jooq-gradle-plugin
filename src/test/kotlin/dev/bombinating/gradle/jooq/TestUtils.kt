package dev.bombinating.gradle.jooq

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File
import java.nio.file.Path
import kotlin.concurrent.timerTask

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
    assertTrue(workspaceDir.toFile("${config.genDir}/${config.packageName.packageToPath()}").exists())
    assertTrue(result.task(":$taskName") != null)
    assertEquals(TaskOutcome.SUCCESS, result.task(":$taskName")?.outcome)
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
    val result = runGradle(workspaceDir, "clean", taskName, "build", "--info")
    validateGradleOutput(workspaceDir = workspaceDir, config = config, result = result, taskName = taskName)
}

fun TestConfig.basicExtensionTest(
    workspaceDir: Path,
    deps: String,
    taskName: String,
    projectName: String = defaultProjectName
) {
    workspaceDir.createSettingsFile(projectName = projectName)
    workspaceDir.createBuildFile(config = this, depBlock = deps) {
        """
            |jooq {
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
    workspaceDir.createSettingsFile(projectName = projectName)
    workspaceDir.createBuildFile(config = this, depBlock = deps) {
        """
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
            |       includes = ".*"
            |   }
            |   target {
            |       directory = genDir
            |       packageName = "$packageName"
            |   }
            |}
            |logging = Logging.TRACE
""".trimIndent()