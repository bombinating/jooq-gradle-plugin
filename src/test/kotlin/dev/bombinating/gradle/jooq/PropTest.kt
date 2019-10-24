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

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.nio.file.Path
import java.sql.DriverManager

class PropTest {

    companion object {

        @Suppress("unused")
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

        @Suppress("unused")
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

        @JvmStatic
        private val config = TestConfig(
            driver = h2Driver,
            schema = defaultSchemaName,
            genDir = defaultGenDir,
            javaVersion = "JavaVersion.VERSION_1_8",
            version = jooqVersion12,
            packageName = defaultPackageName,
            dbGenerator = """includes = ".*""""
        )

        @JvmStatic
        private val deps = dependenciesBlock(
            jooqDependency = jooqOsDependency(group = jooqOsGroup, version = jooqVersion12),
            jdbcDriverDependency = h2JdbcDriverDependency
        )

        class PropConfigProvider : TestConfigProvider(config) {
            /*
             * Properties not supported in < 3.11.x
             */
            override val versions: List<String?> = listOf(jooqVersion11, jooqVersion12, null)
        }

    }

    @TempDir
    lateinit var workspaceDir: Path

    @ParameterizedTest(name = "{index}: {0}")
    @ArgumentsSource(PropConfigProvider::class)
    fun `Task Test`(config: TestConfig) {
        config.basicExtensionTest(workspaceDir = workspaceDir, deps = deps, taskName = defaultJooqTaskName,
            args = *arrayOf("-Djooq.codegen.jdbc.url=$h2Url", "-Djooq.codegen.jdbc.username=$h2Username",
                "-Djooq.codegen.jdbc.password=$h2Password"))
    }

    @ParameterizedTest(name = "{index}: {0}")
    @ArgumentsSource(PropConfigProvider::class)
    fun `Extension Test`(config: TestConfig) {
        val taskName = "jooqTask"
        config.basicTaskTest(workspaceDir = workspaceDir, deps = deps,
            taskName = taskName, args = *arrayOf(
                "-D$taskName.jooq.codegen.jdbc.url=$h2Url",
                "-D$taskName.jooq.codegen.jdbc.username=$h2Username",
                "-D$taskName.jooq.codegen.jdbc.password=$h2Password",
                "-Djooq.codegen.jdbc.password=$h2BadPassword")
        )
    }

}