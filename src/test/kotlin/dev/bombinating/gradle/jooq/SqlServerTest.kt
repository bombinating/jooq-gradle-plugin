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
import org.testcontainers.containers.MSSQLServerContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.nio.file.Path
import java.sql.DriverManager

@EnabledIfEnvironmentVariable(named = envVarProTests, matches = proTestsEnabledValue)
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
                    stmt.execute("create schema [$defaultSchemaName]")
                    stmt.execute("create table [$defaultSchemaName].[$defaultTableName](id int)")
                }
            }
        }

        private val deps = dependenciesBlock(
            jooqDependency = jooqOsDependency(group = jooqOsGroup, version = jooqVersion12),
            jdbcDriverDependency = sqlServerJdbcDriverDependency
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
    fun extTest() {
        config.basicExtensionTest(workspaceDir = workspaceDir, deps = deps, taskName = defaultJooqTaskName, edition = JooqEdition.Pro)
    }

    @Test
    fun taskTest() {
        config.basicTaskTest(workspaceDir = workspaceDir, deps = deps, taskName = "jooqTask", edition = JooqEdition.Pro)
    }

}
