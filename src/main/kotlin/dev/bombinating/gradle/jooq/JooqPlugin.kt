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
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.JavaBasePlugin

/**
 * Gradle entry point into the jOOQ code generation functionality.
 *
 * - ensures that the Java plugin is applied to the project (for compiling the generated code)
 * - creates a `jooqRuntime` configuration so the classpath for the jOOQ code generation tool can be specified
 * - adds the dependencies needed to run the jOOQ code generation tool to the `jooqRuntime` configuration
 * - creates a `jooq` extension that can be used for specifying jooq code generation configurations and one generation
 * - changes any jOOQ dependencies to match the group and version specified
 */
@Suppress("unused")
class JooqPlugin : Plugin<Project> {

    /**
     * Entry point into the jOOQ generation functionality.
     *
     * @param project Gradle Project the jOOQ plugin is applied to
     */
    override fun apply(project: Project) {
        project.plugins.apply(JavaBasePlugin::class.java)
        /*
         * Create the jooqRuntime configuration and set up its dependencies
         */
        val jooqRuntime = project.configurations.create(JOOQ_RUNTIME_NAME).apply {
            description = JOOQ_RUNTIME_DESC
        }

        /*
         * Create the jooq extension
         */
        val jooqExt = project.extensions.create(JOOQ_EXT_NAME, JooqExtension::class.java,
            { version: String -> updateJooqSpringBootVersion(project, version) }, JooqConfigImpl()
        )

        /*
         * Set up the classpath for the code generation.
         */
        project.jooqExt.codeGenDeps.forEach { project.dependencies.add(jooqRuntime.name, it) }

        /*
         * Create the jooq task
         */
        project.tasks.register(JOOQ_TASK_NAME, JooqTask::class.java).get().apply {
            config = jooqExt.config
            jooqClassPath = jooqRuntime
            runConfigLambda = { jooqExt.runConfig }
            resultHandlerLambda = { jooqExt.resultHandler }
        }
        /*
         * Modify jOOQ dependencies to match the version specified by the plugin extension.
         */
        project.configurations.forEach { config ->
            config.resolutionStrategy.eachDependency { dep ->
                val requested = dep.requested
                if (JOOQ_GROUP_IDS.contains(requested.group) && requested.name.startsWith("jooq")) {
                    /*
                     * Change the jOOQ group and version to match the jOOQ extension as necessary.
                     */
                    val oldDep = "${requested.group}:${requested.name}:${requested.version}"
                    val newDep = "${jooqExt.edition.groupId}:${requested.name}:${jooqExt.version}"
                    if (oldDep != newDep) {
                        pluginLogger.info { "Changing '${config.name}' dependency from '$requested' to '$newDep'" }
                        dep.useTarget(newDep)
                    }
                }
            }
        }

    }

    private fun updateJooqSpringBootVersion(project: Project, version: String) {
        if (project.plugins.findPlugin(SPRING_DEP_MAN_PLUGIN_NAME) != null) {
            val ext = (project as ExtensionAware).extensions.getByName(GRADLE_EXT_EXT_NAME) as? ExtraPropertiesExtension
            if (ext != null) {
                pluginLogger.info {
                    """Spring Dependency Management Plugin detected: setting ext["$SPRING_DEP_MAN_JOOQ_VERSION_EXT_NAME"] = "$version""""
                }
                ext[SPRING_DEP_MAN_JOOQ_VERSION_EXT_NAME] = version
            } else {
                pluginLogger.debug { """The 'ext' extension is not present""" }
            }
        } else {
            pluginLogger.debug {
                """Spring Dependency Management Plugin not detected: not setting ext["$SPRING_DEP_MAN_JOOQ_VERSION_EXT_NAME"]"""
            }
        }
    }

}
