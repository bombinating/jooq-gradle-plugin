package dev.bombinating.gradle.jooq

import org.gradle.api.tasks.SourceSet
import org.jooq.meta.jaxb.Configuration

open class JooqExt(val jooqConfigurer: (JooqConfig, JooqExt) -> Unit, val name: String) {

    var version: String = defaultJooqVersion
    var edition: JooqEdition = defaultJooqEdition

    private val configs: MutableMap<String, JooqConfig> = mutableMapOf()

    // FIXME: should we just return the config if it already exists?
    operator fun String.invoke(sourceSet: SourceSet, configurer: Configuration.() -> Unit): JooqConfig {
        val config = Configuration()
        configurer(config)
        val jooqConfig = JooqConfig(name = this, sourceSet = sourceSet, config = config)
        jooqConfigurer(jooqConfig, this@JooqExt)
        configs[this] = jooqConfig
        return jooqConfig
    }

}
