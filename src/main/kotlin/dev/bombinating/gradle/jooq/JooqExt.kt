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
