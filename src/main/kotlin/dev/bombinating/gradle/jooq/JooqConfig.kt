package dev.bombinating.gradle.jooq

import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Logging
import java.io.Serializable

interface JooqConfig : Serializable {
    var jdbc: Jdbc?
    var generator: Generator?
    var logging: Logging?
    val config: Configuration
}

internal class JooqConfigImpl(override val config: Configuration = Configuration()) : JooqConfig {
    override var jdbc: Jdbc?
        get() = config.jdbc
        set(value) {
            config.jdbc = value
        }
    override var generator: Generator?
        get() = config.generator
        set(value) {
            config.generator = value
        }
    override var logging: Logging?
        get() = config.logging
        set(value) {
            config.logging = value
        }
}

