package dev.bombinating.gradle.jooq

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.sql.DriverManager

class H2Test {

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
        config.basicExtensionTest(workspaceDir = workspaceDir, deps = deps, taskName = defaultJooqTaskName)
    }

    @Test
    fun taskTest() {
        config.basicTaskTest(workspaceDir = workspaceDir, deps = deps, taskName = "jooqTask")
    }

}