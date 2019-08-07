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

import org.jooq.meta.jaxb.*
import org.jooq.meta.jaxb.Target

internal const val defaultJooqVersion = "3.11.11"
internal const val jooqConfigFilename = "config.xml"
internal const val jooqRuntimeName = "jooqRuntime"
internal const val jooqRuntimeDesc =
    "The classpath used to invoke the jOOQ generator. Add your JDBC drivers or generator extensions here."
internal const val jooqExtName = "jooq"
internal const val jooqTaskGroupName = jooqExtName
internal const val jooqTaskDesc = "Generates the jOOQ configuration"
internal val jooqCodeGenDeps = listOf(
    "org.jooq:jooq-codegen:$defaultJooqVersion",
    "javax.xml.bind:jaxb-api:2.3.1",
    "javax.activation:activation:1.1.1",
    "com.sun.xml.bind:jaxb-core:2.3.0.1",
    "com.sun.xml.bind:jaxb-impl:2.3.0.1"
)
internal val defaultJooqEdition = JooqEdition.OpenSource
//internal fun String.outputDirName() = "generated/src/main/java"

fun Configuration.jdbc(action: Jdbc.() -> Unit) {
    jdbc = (jdbc ?: Jdbc()).apply(action)
}

fun Configuration.generator(action: Generator.() -> Unit) {
    generator = (generator ?: Generator()).apply(action)
}

fun Generator.database(action: Database.() -> Unit) {
    database = ((database ?: Database()).apply(action))
}

fun Generator.target(action: Target.() -> Unit) {
    target = ((target ?: Target()).apply(action))
}

fun Generator.strategy(action: Strategy.() -> Unit) {
    strategy = ((strategy ?: Strategy()).apply(action))
}

fun Strategy.matchers(action: Matchers.() -> Unit) {
    matchers = ((matchers ?: Matchers()).apply(action))
}

fun Matchers.tables(action: MutableList<MatchersTableType>.() -> Unit) {
    tables = ((tables ?: mutableListOf()).apply(action))
}

fun MutableList<MatchersTableType>.table(action: MatchersTableType.() -> Unit) {
    this += MatchersTableType().apply(action)
}

fun MatchersTableType.pojoClass(action: MatcherRule.() -> Unit) {
    pojoClass = ((pojoClass ?: MatcherRule()).apply(action))
}

fun Generator.generate(action: Generate.() -> Unit) {
    generate = ((generate ?: Generate()).apply(action))
}

fun Database.forcedTypes(action: MutableList<ForcedType>.() -> Unit) {
    forcedTypes = ((forcedTypes ?: mutableListOf()).apply(action))
}

fun MutableList<ForcedType>.forcedType(action: ForcedType.() -> Unit) {
    this += ForcedType().apply(action)
}
