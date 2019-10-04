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

/**
 * jOOQ code generation Gradle plugin extension.
 *
 * @property name name of the Gradle extension
 * @property jooqConfigurer lambda for configuring individual jOOQ tasks after they are created
 * @property version jOOQ library version to use
 * @property edition jOOQ library edition to use
 * @property compileDep whether to make the jOOQ code generation a dependency for the Java compile task
 * @property runConfig customization of the Java runtime that will be used to run the jOOQ code generation tool
 * @property resultHandler lambda for handling the result of executing the jOOQ code generation tool
 *
 * Usage in build.gradle.kts:
 *
 * ```
 * jooq {
 *      version = "3.11.11"
 *      edition = JooqEdition.OpenSource
 *      compileDep = true
 *      runConfig = {
 *          isIgnoreExitValue = true
 *           jvmArgs("-Xmx512M")
 *           standardOutput = System.out
 *           errorOutput = System.out
 *      }
 *      resultHandler = {
 *           ...
 *      }
 *      "main"(sourceSets["main"]) {
 *          ...
 *      }
 * }
 * ```
 */
open class JooqExt(val config: Configuration.() -> Unit)
