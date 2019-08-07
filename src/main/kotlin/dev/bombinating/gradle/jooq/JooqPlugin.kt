package dev.bombinating.gradle.jooq

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Target

class JooqPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply(JavaBasePlugin::class.java)
        val jooqRuntime = project.configurations.create(jooqRuntimeName).apply {
            description = jooqRuntimeDesc
        }
        jooqCodeGenDeps.forEach {
            project.dependencies.add(jooqRuntime.name, it)
        }
        val configurer: (JooqConfig, JooqExt) -> Unit = { config, ext ->
            project.tasks.create(config.jooqTaskName, JooqTask::class.java, config.config, jooqRuntime)
            config.sourceSet.let {
                it.java.srcDir { config.config.generator.target.directory }
                project.tasks.getByName(it.compileJavaTaskName).dependsOn(config.jooqTaskName)
            }
            project.configurations.forEach {
                it.resolutionStrategy.eachDependency { details ->
                    val requested = details.requested
                    if (JooqEdition.groupIds.contains(requested.group)) {
                        details.useTarget("${ext.edition.groupId}:${requested.name}:${ext.version}")
                    }
                }
            }
        }
        project.extensions.create(jooqExtName, JooqExt::class.java, configurer, jooqExtName, project)
    }

}
