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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.test.assertTrue

class BadDslArgumentProvider : ArgumentsProvider {
    private val badElement = "logging = Logging.INFO"
    private val badBlocks = listOf(
        "jdbc { $badElement }",
        "jdbc { properties { $badElement } }",
        "generator { $badElement }",
        "generator { target { $badElement } }",
        "generator { database { $badElement } }",
        "generator { generate { $badElement } }",
        "generator { strategy { $badElement } }",
        "generator { strategy { matchers { $badElement } } }",
        "generator { strategy { matchers { tables { $badElement } } } }",
        "generator { strategy { matchers { tables { table { $badElement } } } } }",
        "generator { strategy { matchers { tables { table { pojoClass { $badElement } } } } } }",
        "generator { database { catalogs { $badElement } } }",
        "generator { database { catalogs { catalogMappingType { $badElement } } } }",
        "generator { database { catalogs { catalogMappingType { schemata { $badElement } } } } }",
        "generator { database { catalogs { catalogMappingType { schemata { schemataMappingType { $badElement } } } } } } }",
        "generator { database { embeddables { $badElement } } }",
        "generator { database { embeddables { embeddable { $badElement } } } }",
        "generator { database { embeddables { embeddable { field { $badElement } } } } }",
        "generator { database { enumTypes { $badElement } } }",
        "generator { database { enumTypes { enumType { $badElement } } } }",
        "generator { database { forcedTypes { $badElement } } }",
        "generator { database { forcedTypes { forcedType { $badElement } } } }",
        "generator { database { properties { $badElement } } }",
        "generator { database { properties { property { $badElement } } } }"
    )

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> =
        badBlocks.map { Arguments.of(it) }.stream()
}

class DslTest {

    private val expectedError =
        "'var logging: Logging?' can't be called in this context by implicit receiver. Use the explicit one if necessary"

    private val config = TestConfig(
        driver = h2Driver,
        url = h2Url,
        username = h2Username,
        password = h2Password,
        schema = defaultSchemaName,
        genDir = defaultGenDir,
        javaVersion = "JavaVersion.VERSION_1_8",
        version = jooqVersion13,
        packageName = defaultPackageName,
        dbGenerator = """includes = ".*""""
    )

    @TempDir
    lateinit var workspaceDir: Path

    @ParameterizedTest(name = "{index}: {0}")
    @ArgumentsSource(BadDslArgumentProvider::class)
    fun `Bad DSL extension Test`(badBlock: String) {
        val taskName = "clean"
        workspaceDir.createPropFile()
        workspaceDir.createSettingsFile(projectName = defaultProjectName)
        workspaceDir.createBuildFile(config = config, depBlock = "") {
            """jooq {
                |   $badBlock
                |}
            """.trimIndent()
        }
        val e = assertThrows<UnexpectedBuildFailure> {
            runGradle(workspaceDir, taskName)
        }
        assertTrue { e.localizedMessage.contains(expectedError) }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @ArgumentsSource(BadDslArgumentProvider::class)
    fun `Bad DSL task Test`(badBlock: String) {
        val taskName = "clean"
        workspaceDir.createPropFile()
        workspaceDir.createSettingsFile(projectName = defaultProjectName)
        workspaceDir.createBuildFile(config = config, depBlock = "") {
            """tasks.register<JooqTask>("jooq2") { 
            |   $badBlock
            """.trimIndent()
        }
        val e = assertThrows<UnexpectedBuildFailure> {
            runGradle(workspaceDir, taskName, "--info", "--stacktrace")
        }
        assertTrue { e.localizedMessage.contains(expectedError) }
    }
}
