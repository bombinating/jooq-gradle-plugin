package dev.bombinating.gradle.jooq

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.sql.DriverManager

class GenerateTest {

    companion object {

        @BeforeAll
        @JvmStatic
        fun setup() {
            DriverManager.getConnection(h2Url, h2Username, h2Password).use { conn ->
                conn.createStatement().use { stmt ->
                    /*
                     * Create the schema.
                     */
                    stmt.execute("create schema if not exists $defaultSchemaName")
                    /*
                     * Create the table in the schema.
                     */
                    stmt.execute("create table if not exists $defaultSchemaName.$defaultTableName(id int)")
                }
            }
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            DriverManager.getConnection(h2Url, h2Username, h2Password).use { conn ->
                conn.createStatement().use { stmt ->
                    /*
                     * Stop the database.
                     */
                    stmt.execute("SHUTDOWN")
                }
            }
        }

        private val config = TestConfig(
            driver = h2Driver,
            url = h2Url,
            username = h2Username,
            password = h2Password,
            schema = defaultSchemaName,
            genDir = defaultGenDir,
            javaVersion = "JavaVersion.VERSION_1_8",
            jooqVersion = jooqVersion12,
            packageName = defaultPackageName
        )

        private val deps = dependenciesBlock(
            jooqDependency = jooqOsDependency(group = jooqOsGroup, version = jooqVersion12),
            jdbcDriverDependency = h2JdbcDriverDependency
        )

    }

    @TempDir
    lateinit var workspaceDir: Path

    @Test
    fun extTest() {
        workspaceDir.createSettingsFile(projectName = defaultProjectName)
        workspaceDir.createBuildFile(config = config, depBlock = deps) {
            """
            |jooq {
            |   jdbc {
            |       driver = "$driver"
            |       url = "$url"
            |       user = "$username"
            |       password = "$password"
            |   }
            |   generator {
            |       database {
            |           name = "org.jooq.meta.h2.H2Database"
            |           includes = ".*"
            |       }
            |       target {
            |           directory = genDir
            |           packageName = "$packageName"
            |       }
            |   }
            |   logging = Logging.TRACE
            |}
            """.trimMargin("|")
        }
        runGradleAndValidate(workspaceDir = workspaceDir, config = config, taskName = defaultJooqTaskName)
    }

    @Test
    fun taskTest() {
        val taskName = "jooqTask"
        workspaceDir.createSettingsFile(projectName = defaultProjectName)
        workspaceDir.createBuildFile(config = config, depBlock = deps) {
            """
            |tasks.register<JooqTask>("$taskName") {
            |   jdbc {
            |       driver = "$driver"
            |       url = "$url"
            |       user = "$username"
            |       password = "$password"
            |   }
            |   generator {
            |       database {
            |           name = "org.jooq.meta.h2.H2Database"
            |           includes = ".*"
            |       }
            |       target {
            |           directory = genDir
            |           packageName = "$packageName"
            |       }
            |   }
            |   logging = Logging.TRACE
            |}
            """.trimMargin("|")
        }
        runGradleAndValidate(workspaceDir = workspaceDir, config = config, taskName = taskName)
    }

    private fun validateGradleOutput(config: TestConfig, result: BuildResult, taskName: String) {
        assertTrue(workspaceDir.toFile("${config.genDir}/${config.packageName.packageToPath()}").exists())
        assertTrue(result.task(":$taskName") != null)
        assertEquals(TaskOutcome.SUCCESS, result.task(":$taskName")?.outcome)
    }

    private fun runGradle(workspaceDir: Path, vararg args: String): BuildResult {
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

    private fun runGradleAndValidate(workspaceDir: Path, config: TestConfig, taskName: String) {
        val result = runGradle(workspaceDir, "clean", taskName, "build", "--info")
        validateGradleOutput(config = config, result = result, taskName = taskName)
    }

}