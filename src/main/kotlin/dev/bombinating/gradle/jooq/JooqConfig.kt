package dev.bombinating.gradle.jooq

import org.gradle.api.Named
import org.gradle.api.tasks.SourceSet
import org.jooq.meta.jaxb.Configuration

class JooqConfig(name: String, val sourceSet: SourceSet, val config: Configuration) : Named {

    private val name: String = name

    override fun getName(): String = name

    val jooqTaskName: String
        get() = "generate${name.capitalize()}Jooq"
}
