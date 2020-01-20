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

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.io.TempDir
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.nio.file.Path
import java.sql.DriverManager
import kotlin.test.assertFalse

/**
 * This test uses Spring Boot 2.2.0.RELEASE, which specifies the jOOQ version to be 3.12.1.
 *
 * For PostgreSQL 12, version 3.12.1 of the jooq-meta library logs a SQL error about not being able to read the list of
 * Pg domains [https://github.com/jOOQ/jOOQ/issues/9334]. This was fixed in version 3.12.2 of the jooq-meta library.
 *
 * However, even when the jOOQ plugin specified that it should use the 3.12.2 version, the Spring Dependency Management
 * Plugin [https://docs.spring.io/dependency-management-plugin/docs/current/reference/html/] overwrote the
 * _transitive_ dependencies of the jOOQ plugin, based on the fact that the jOOQ version configured in Spring Boot
 * 2.2.0.RELEASE was 3.12.1.
 *
 * That is, the Dependency Management Plugin would not change the versions of the jOOQ library that were specifically
 * specified (e.g., jooq-codegen) but _would_ change any non-specified, transitive dependencies, based on the version
 * of jOOQ configured in Spring Boot (e.g., jooq-meta 3.12.2 is a transitive dependency of jooq-codegen 3.12.2 and the
 * Dependency Management Plugin changed its version to 3.12.1 because the version of jOOQ in Spring Boot 2.2.0.RELEASE
 * was 3.12.1).
 *
 * The tests in this class verify that the jOOQ plugin is properly changing the versions of all of the jOOQ libraries
 * by checking that the error message in jooq-meta 3.12.1 for Pg 12 is not present.
 */
@EnabledIfEnvironmentVariable(named = envVarContainerTests, matches = containerEnabledValue)
@Testcontainers
class SpringBootTest {

    companion object {

        @Container
        private val db = PostgreSQLContainer<Nothing>("postgres:12.0")

        @Suppress("unused")
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

        @JvmStatic
        private val deps = dependenciesBlock(
            jooqDependency = jooqOsDependency(group = jooqOsGroup, version = jooqVersion12),
            jdbcDriverDependency = pgJdbcDriverDependency
        )

        @JvmStatic
        private val config: TestConfig
            get() = TestConfig(
                driver = db.driverClassName,
                url = db.jdbcUrl,
                username = db.username,
                password = db.password,
                schema = defaultSchemaName,
                genDir = defaultGenDir,
                javaVersion = "JavaVersion.VERSION_1_8",
                version = jooqVersion12,
                packageName = defaultPackageName,
                dbGenerator = """includes = "$defaultSchemaName.*"""",
                additionalPlugins = """id("org.springframework.boot") version "2.2.0.RELEASE"""",
                additionalConfig = """apply(plugin = "io.spring.dependency-management")"""
            )

        class PgConfigProvider : TestConfigProvider(config)

    }

    @TempDir
    lateinit var workspaceDir: Path

    @Test
    fun `Test Spring Dependency Management Plugin does not change jOOQ Version for jOOQ Plugin`() {
        workspaceDir.createPropFile()
        workspaceDir.createSettingsFile(projectName = defaultProjectName)
        workspaceDir.createBuildFile(config = config, depBlock = deps) { createJooqExtBlockWithConfig() }
        val result = runGradle(workspaceDir, "clean", "jooq", "--info")
        assertFalse("Spring dependency plugin overriding the jOOQ version specified in the jOOQ plugin") {
            result.output.contains("DataAccessException")
        }
        validateGradleOutput(workspaceDir = workspaceDir, config = config, result = result, taskName = "jooq")
    }

}
