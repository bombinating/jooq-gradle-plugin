package dev.bombinating.gradle.jooq

import org.apache.log4j.LogManager
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.sql.DriverManager

class GenerateTest {

    companion object {

        @BeforeAll
        @JvmStatic
        fun setup() {
            DriverManager.getConnection(h2Url, h2Username, h2Password).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("create schema if not exists $defaultSchemaName")
                    stmt.execute("create table if not exists $defaultSchemaName.$defaultTableName(id int)")
                }
            }
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            DriverManager.getConnection(h2Url, h2Username, h2Password).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("SHUTDOWN")
                }
            }
        }

    }

    @TempDir
    lateinit var workspaceDir: Path

    @Test
    fun extTest() {
        val config = TestConfigInfo(
            driver = h2Driver,
            url = h2Url,
            username = h2Username,
            password = h2Password,
            schema = defaultSchemaName,
            genDir = defaultGenDir,
            javaVersion = "JavaVersion.VERSION_1_8",
            jooqVersion = jooqVersion12,
            jooqPluginVersion = "xxx",
            packageName = defaultPackageName
        )
        val settings = workspaceDir.createSettingsFile(projectName = defaultProjectName)
        val build = workspaceDir.createBuildFile(
            config = config,
            depBlock = dependenciesBlock(
                jooqDependency = jooqOsDependency(group = jooqOsGroup, version = jooqVersion12),
                jdbcDriverDependency = h2JdbcDriverDependency
            )
        ) {
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

        val result = GradleRunner.create()
            .withPluginClasspath()
            .withArguments(defaultJooqTaskName, "--info")
            .withProjectDir(workspaceDir.toFile())
            .forwardOutput()
            .build()

        Assertions.assertTrue(workspaceDir.toFile("${config.genDir}/${config.packageName.packageToPath()}").exists())
        Assertions.assertTrue(result.task(":$defaultJooqTaskName") != null)
        Assertions.assertEquals(TaskOutcome.SUCCESS, result.task(":$defaultJooqTaskName")?.outcome)
    }

}