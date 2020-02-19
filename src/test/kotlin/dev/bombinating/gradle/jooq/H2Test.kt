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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class H2Test : AbstractH2Test() {

    companion object {

        @JvmStatic
        private val deps = dependenciesBlock(
            jooqDependency = jooqOsDependency(group = jooqOsGroup, version = jooqVersion13),
            jdbcDriverDependency = h2JdbcDriverDependency
        )

        class H2ConfigProvider : TestConfigProvider(h2Config)

    }

    @ParameterizedTest(name = "{index}: {0}")
    @ArgumentsSource(H2ConfigProvider::class)
    fun `Task Test`(config: TestConfig) {
        config.basicExtensionTest(workspaceDir = workspaceDir, deps = deps, taskName = defaultJooqTaskName)
    }

    @ParameterizedTest(name = "{index}: {0}")
    @ArgumentsSource(H2ConfigProvider::class)
    fun `Extension Test`(config: TestConfig) {
        config.basicTaskTest(workspaceDir = workspaceDir, deps = deps,
            taskName = "jooqTask")
    }

}