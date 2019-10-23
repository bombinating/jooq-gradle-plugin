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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnJre
import org.junit.jupiter.api.condition.JRE
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.sql.DriverManager

// NOTE: Gradle 5.6.x does not support Groovy on Java 13: https://github.com/gradle/gradle/issues/10785
@EnabledOnJre(JRE.JAVA_8,JRE.JAVA_11)
class GroovyTest {

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
            url = h2Url,
            username = h2Username,
            password = h2Password,
            schema = defaultSchemaName,
            genDir = defaultGenDir,
            javaVersion = "JavaVersion.VERSION_1_8",
            version = jooqVersion12,
            packageName = defaultPackageName,
            dbGenerator = """includes = ".*""""
        )

        @JvmStatic
        private val deps = groovyDependenciesBlock(
            jooqDependency = jooqGroovyOsDependency(group = jooqOsGroup, version = jooqVersion12),
            jdbcDriverDependency = h2GroovyJdbcDriverDependency
        )

    }

    @TempDir
    lateinit var workspaceDir: Path

    @Test
    fun `Groovy jOOQ Extension Test`() {
        val taskName = "clean"
        workspaceDir.createPropFile()
        workspaceDir.createSettingsFile(projectName = defaultProjectName)
        workspaceDir.createGroovyBuildFile(config = config, depBlock = deps) {
            """ |jooq {
                |   use(ConfigExtKt) {
                |       version = "$jooqVersion12"
                |       jdbc {
                |           it.url = "$h2Url"
                |           it.username = "$h2Username"
                |           it.password = "$h2Password"
                |       }
                |       generator {
                |           it.database {
                |               it.includes = ".*"
                |           }
                |           it.target {
                |               it.directory = genDir
                |               it.packageName = "$defaultPackageName"
                |           }
                |       }
                |       logging = Logging.DEBUG
                |   }
                |}""".trimMargin()
        }
        runGradleAndValidate(workspaceDir = workspaceDir, config = config, taskName = "jooq")
    }

}
