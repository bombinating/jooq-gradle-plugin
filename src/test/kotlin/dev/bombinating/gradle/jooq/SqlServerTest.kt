package dev.bombinating.gradle.jooq

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.testcontainers.containers.MSSQLServerContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.nio.file.Path
import java.sql.DriverManager

@Disabled("Requires docker container to run")
@Testcontainers
class SqlServerTest {

    companion object {

        @Container
        private val db = MSSQLServerContainer<Nothing>("mcr.microsoft.com/mssql/server:2017-CU12")

        @BeforeAll
        @JvmStatic
        fun setup() {
            DriverManager.getConnection(db.jdbcUrl, db.username, db.password).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("create database test")
                    stmt.execute("create table [test].[dbo].[acme](id int)")
                }
            }
        }

    }

    @TempDir
    lateinit var workspaceDir: Path

    @Test
    fun baseTest() {
        assertTrue(db.isRunning)
        val confName = "sql"
        workspaceDir.createSettings()
        workspaceDir.createBuild(
            genDir = GEN_DIR,
            depBlock = """
                compile(group = "org.jooq.pro", name = "jooq", version = "$JOOQ_VERSION")
                jooqRuntime("com.microsoft.sqlserver:mssql-jdbc:7.4.1.jre8")""".trimIndent()
        ) {
            """
            edition = JooqEdition.Professional
            "$confName"(sourceSets["main"]) {
                jdbc {
                    driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
                    url = "${db.jdbcUrl}"
                    user = "${db.username}"
                    password = "${db.password}"
                }
                generator {
                    database {
                        includes = ".*"
                        inputCatalog = "test"
                        inputSchema = "dbo"
                    }
                    target {
                        directory = genDir
                        packageName = "$PACKAGE_NAME"
                    }
                }
                logging = Logging.TRACE
            }
            """.trimIndent()
        }

        val result = GradleRunner.create()
            .withPluginClasspath()
            .withArguments("build")
            .withProjectDir(workspaceDir.toFile())
            .forwardOutput()
            .build()
        assertTrue(
            File(
                workspaceDir.toFile(),
                "$GEN_DIR/${PACKAGE_NAME.replace(".", "/")}/tables/Acme.java"
            ).exists()
        )
        assertTrue(result.task(":generate${confName.capitalize()}Jooq") != null)
    }
}
