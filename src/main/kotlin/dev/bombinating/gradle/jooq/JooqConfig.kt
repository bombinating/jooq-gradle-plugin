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

