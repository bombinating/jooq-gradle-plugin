package dev.bombinating.gradle.jooq

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
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
                    stmt.execute("create schema $defaultSchemaName")
                    stmt.execute("create table $defaultSchemaName.$defaultTableName(id int)")
                }
            }
        }

        private val deps = dependenciesBlock(
            jooqDependency = jooqOsDependency(group = jooqOsGroup, version = jooqVersion12),
            jdbcDriverDependency = pgJdbcDriverDependency
        )

    }

    @TempDir
    lateinit var workspaceDir: Path

    private val config = TestConfig(
        driver = db.driverClassName,
        url = db.jdbcUrl,
        username = db.username,
        password = db.password,
        schema = defaultSchemaName,
        genDir = defaultGenDir,
        javaVersion = "JavaVersion.VERSION_1_8",
        jooqVersion = jooqVersion12,
        packageName = defaultPackageName,
        includes = "test.*"
    )

    @Test
    fun baseExtTest() {
        config.basicExtensionTest(workspaceDir = workspaceDir, deps = deps, taskName = defaultJooqTaskName)
    }

    @Test
    fun baseTaskTest() {
        config.basicTaskTest(workspaceDir = workspaceDir, deps = deps, taskName = "jooqTask")
    }

}
