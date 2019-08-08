package dev.bombinating.gradle.jooq

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.nio.file.Path
import java.sql.DriverManager

@Testcontainers
class PgTest {

    companion object {

        @Container
        private val db = PostgreSQLContainer<Nothing>("postgres:11.2")

        @BeforeAll
        @JvmStatic
        fun setup() {
            DriverManager.getConnection(db.jdbcUrl, db.username, db.password).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("create table acme(id int)")
                }
            }
        }

    }

    @TempDir
    lateinit var workspaceDir: Path

    @Test
    fun baseTest() {
        assertTrue(db.isRunning)
        val confName = "pg"
        workspaceDir.createSettings()
        workspaceDir.createBuild(
            genDir = GEN_DIR,
            depBlock = """
                compile(group = "org.jooq", name = "jooq", version = "$JOOQ_VERSION")
                jooqRuntime("org.postgresql:postgresql:42.2.6")""".trimIndent()
        ) {
            """
            "pg"(sourceSets["main"]) {
                jdbc {
                    url = "${db.jdbcUrl}"
                    user = "${db.username}"
                    password = "${db.password}"
                    schema = "public"
                }
                generator {
                    database {
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
                "$GEN_DIR/${PACKAGE_NAME.replace(".", "/")}/tables/Acme.java"
            ).exists()
        )
        assertTrue(result.task(":generate${confName.capitalize()}Jooq") != null)
    }

}
