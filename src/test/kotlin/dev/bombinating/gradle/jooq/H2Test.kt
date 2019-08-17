package dev.bombinating.gradle.jooq

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.sql.DriverManager

class H2Test {

    companion object {

        private val URL = "jdbc:h2:~/test_db;AUTO_SERVER=true"
        private val USER = "sa"
        private val PASSWORD = ""
        private val SCHEMA = "test"

        @BeforeAll
        @JvmStatic
        fun setup() {
            DriverManager.getConnection(URL, USER, PASSWORD).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("create schema if not exists $SCHEMA")
                    stmt.execute("create table if not exists $SCHEMA.acme(id int)")
                }
            }
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            DriverManager.getConnection(URL, USER, PASSWORD).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("SHUTDOWN")
                }
            }
        }

    }

    @TempDir
    lateinit var workspaceDir: Path

    @Test
    fun baseTest() {
        val confName = "h2"
        workspaceDir.createSettings()
        workspaceDir.createBuild(
            genDir = GEN_DIR,
            depBlock = """
                compile(group = "org.jooq", name = "jooq", version = "$JOOQ_VERSION")
                jooqRuntime("com.h2database:h2:1.4.199")""".trimIndent()
        ) {
            """
            "$confName"(sourceSets["main"]) {
                jdbc {
                    driver = "org.h2.Driver"
                    url = "$URL"
                    user = "$USER"
                    password = "$PASSWORD"
                }
                generator {
                    database {
                        name = "org.jooq.meta.h2.H2Database"
                        includes = ".*"
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
                "$GEN_DIR/${PACKAGE_NAME.replace(".", "/")}/test/tables/Acme.java"
            ).exists()
        )
        assertTrue(result.task(":generate${confName.capitalize()}Jooq") != null)
    }

}
