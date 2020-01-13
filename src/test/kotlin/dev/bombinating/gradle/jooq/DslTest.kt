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

import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class DslTest {

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

    @TempDir
    lateinit var workspaceDir: Path

    @Test
    fun `Bad DSL extension Test`() {
        val taskName = "clean"
        workspaceDir.createPropFile()
        workspaceDir.createSettingsFile(projectName = defaultProjectName)
        workspaceDir.createBuildFile(config = config, depBlock = "") {
            """jooq {
            |   generator {
            |       target {
            |           jdbc {
            |           }
            |       }
            |   }
            |}""".trimMargin()
        }
        assertThrows<UnexpectedBuildFailure> {
            runGradle(workspaceDir, taskName, "--info", "--stacktrace")
        }
    }

    @Test
    fun `Bad DSL task Test`() {
        val taskName = "clean"
        workspaceDir.createPropFile()
        workspaceDir.createSettingsFile(projectName = defaultProjectName)
        workspaceDir.createBuildFile(config = config, depBlock = "") {
            """tasks.register<JooqTask>("jooq2") { 
            |   generator {
            |       target {
            |           jdbc {
            |           }
            |       }
            |   }
            |}""".trimMargin()
        }
        assertThrows<UnexpectedBuildFailure> {
            runGradle(workspaceDir, taskName, "--info", "--stacktrace")
        }
    }
}
