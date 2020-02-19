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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnJre
import org.junit.jupiter.api.condition.JRE

// NOTE: Gradle 5.6.x does not support Groovy on Java 13: https://github.com/gradle/gradle/issues/10785
@EnabledOnJre(JRE.JAVA_8,JRE.JAVA_11)
class GroovyTest : AbstractH2Test() {

    companion object {

        @JvmStatic
        private val deps = groovyDependenciesBlock(
            jooqDependency = jooqGroovyOsDependency(group = jooqOsGroup, version = jooqVersion13),
            jdbcDriverDependency = h2GroovyJdbcDriverDependency
        )

    }

    @Test
    fun `Groovy jOOQ Extension Test`() {
        workspaceDir.createPropFile()
        workspaceDir.createSettingsFile(projectName = defaultProjectName)
        workspaceDir.createGroovyBuildFile(config = h2Config, depBlock = deps) {
            """ |jooq {
                |   use(ConfigExtKt) {
                |       version = "$jooqVersion13"
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
                |           ${createGenerateBlock(groovy = true).prependIndent("\t")}
                |       }
                |       logging = Logging.DEBUG
                |   }
                |}""".trimMargin()
        }
        runGradleAndValidate(workspaceDir = workspaceDir, config = h2Config, taskName = "jooq")
    }

}
