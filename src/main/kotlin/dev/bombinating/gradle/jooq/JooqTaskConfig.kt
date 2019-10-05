package dev.bombinating.gradle.jooq

import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Logging

/**
 * jOOQ Gradle task configuration. Has the same top-level properties as the [Configuration] class.
 *
 * @property config jOOQ [Configuration] associated with the Gradle config
 * @property jdbc jOOQ [Configuration] JDBC info
 * @property generator jOOQ [Configuration] generator info
 * @property logging jOOQ [Configuration] logging level
 *
 * Usage in build.gradle.kts:
 *
 * ```
 * tasks.create<JooqTask>("accounting") {
 *      jdbc {
 *          driver = "org.h2.Driver"
 *          url = "jdbc:h2:~/test_db;AUTO_SERVER=true"
 *          user = "sa"
 *          password = ""
 *      }
 *      generator {
 *          database {
 *              name = "org.jooq.meta.h2.H2Database"
 *              includes = ".*"
 *          }
 *          target {
 *              directory = "$projectDir/generated/src/main/java"
 *              packageName = "com.acme.jooq"
 *          }
 *      }
 * }
 * ```
 */
open class JooqTaskConfig {
    val config: Configuration = Configuration()

    var jdbc: Jdbc?
        get() = config.jdbc
        set(jdbc) {
            config.jdbc = jdbc
        }

    var generator: Generator?
        get() = config.generator
        set(generator) {
            config.generator = generator
        }

    var logging: Logging?
        get() = config.logging
        set(logging) {
            config.logging = logging
        }

}
