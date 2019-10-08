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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin

/**
 * Entry point into the jOOQ code generation functionality.
 *
 * - ensures that the Java plugin is applied to the project (for compiling the generated code)
 * - creates a `jooqRuntime` configuration so the classpath for the jOOQ code generation tool can be specified
 * - adds the dependencies needed to run the jOOQ code generation tool to the `jooqRuntime` configuration
 * - creates a `jooq` extension that can be used for specifying jooq code generation configurations
 * - the jOOQ code generation configurations are turned into Gradle tasks with the name generate<ConfigName>Jooq
 * - in addition, there is an additional task, generateJooq, that all of the individual jOOQ code gen tasks depend on
 */
class JooqPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply(JavaBasePlugin::class.java)
        val jooqRuntime = project.configurations.create(JOOQ_RUNTIME_NAME).apply {
            description = JOOQ_RUNTIME_DESC
        }
        JOOQ_CODE_GEN_DEPS.forEach { project.dependencies.add(jooqRuntime.name, it) }
        project.extensions.create(JOOQ_EXT_NAME, JooqExtConfig::class.java)
        val x = project.extensions.findByName(JOOQ_EXT_NAME) as JooqConfig
        val task = project.tasks.register(
            JOOQ_TASK_NAME,
            JooqGenerateTask::class.java //,
//            x.config,
//            x,
//            jooqRuntime
        )
        task.get().config = x.config
        task.get().jooqClassPath = jooqRuntime
    }

}
