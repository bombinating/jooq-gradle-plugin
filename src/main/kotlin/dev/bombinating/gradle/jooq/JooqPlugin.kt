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

class JooqPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply(JavaBasePlugin::class.java)
        val jooqRuntime = project.configurations.create(jooqRuntimeName).apply {
            description = jooqRuntimeDesc
        }
        jooqCodeGenDeps.forEach { project.dependencies.add(jooqRuntime.name, it) }
        val configurer: (JooqConfig, JooqExt) -> Unit = { config, ext ->
            project.tasks.create(config.jooqTaskName, JooqTask::class.java, config.config, jooqRuntime)
            config.sourceSet.let {
                it.java.srcDir { config.config.generator.target.directory }
                project.tasks.getByName(it.compileJavaTaskName).dependsOn(config.jooqTaskName)
            }
            project.configurations.forEach {
                it.resolutionStrategy.eachDependency { details ->
                    val requested = details.requested
                    if (JooqEdition.jooqGroupIds.contains(requested.group)) {
                        details.useTarget("${ext.edition.groupId}:${requested.name}:${ext.version}")
                    }
                }
            }
        }
        project.extensions.create(jooqExtName, JooqExt::class.java, configurer, jooqExtName)
    }

}
