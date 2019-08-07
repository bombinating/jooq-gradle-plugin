package dev.bombinating.gradle.jooq

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.jooq.meta.jaxb.Configuration
import org.gradle.kotlin.dsl.*

open class JooqExt(val jooqConfigurer: (JooqConfig, JooqExt) -> Unit, val name: String, val project: Project) {

    var version: String = defaultJooqVersion
    var edition: JooqEdition = defaultJooqEdition

    private val configs: MutableMap<String, JooqConfig> = mutableMapOf()

    operator fun String.invoke(sourceSet: SourceSet = project.sourceSets["main"], configurer: Configuration.() -> Unit): JooqConfig {
        val config = Configuration()
        configurer(config)
        val jooqConfig = JooqConfig(name = this, sourceSet = sourceSet, config = config)
        jooqConfigurer(jooqConfig, this@JooqExt)
        configs[this] = jooqConfig
        return jooqConfig
    }

}
