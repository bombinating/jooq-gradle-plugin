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

/**
 * jOOQ Gradle extension configuration, which implements [JooqConfig].
 *
 * @property version version of the jOOQ library to use; if not specified, defaults to the latest version
 * @property edition edition of the jOOQ library to use; if not specified, defaults to the open source edition
 * @property runConfig JVM config for running the jOOQ `GenerationTool`
 * @property resultHandler Result handler for result for running jOOQ `GenerationTool`
 *
 * Usage in build.gradle.kts:
 *
 * ```
 * jooq {
 *      version = "3.12.4"
 *      edition = JooqEdition.OpenSource
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
 *      logging = Logging.TRACE
 * }
 * ```
 */
@JooqDsl
open class JooqExtension(
    private val versionChangeLambda: ((String) -> Unit)? = null,
    private val jooqConfig: JooqConfig = JooqConfigImpl()) : JooqConfig by jooqConfig
{
    var edition: JooqEdition = DEFAULT_JOOQ_EDITION
    private var _version: String = DEFAULT_JOOQ_VERSION
    var version: String
        get() = _version
        set(version) {
            _version = version
            versionChangeLambda?.invoke(version)
        }

    override fun toString(): String {
        return """version: $version, edition: $edition, config: $jooqConfig"""
    }

    internal val jooqVersion: JooqVersion
        get() = version.toJooqVersion()

}
